package org.metaborg.sunshine.services.pipelined.builders;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.context.ContextIdentifier;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageIdentifierService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.ILanguageService;
import org.metaborg.core.resource.ResourceService;
import org.metaborg.spoofax.core.context.SpoofaxContext;
import org.metaborg.spoofax.core.menu.MenuService;
import org.metaborg.spoofax.core.menu.StrategoTransformAction;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.stratego.StrategoRuntimeUtils;
import org.metaborg.sunshine.environment.LaunchConfiguration;
import org.metaborg.sunshine.environment.ServiceRegistry;
import org.metaborg.sunshine.pipeline.ISinkOne;
import org.metaborg.sunshine.pipeline.diff.Diff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.HybridInterpreter;

import com.google.common.collect.Iterables;

public class BuilderSink implements ISinkOne<BuilderInputTerm> {
    private static final Logger logger = LoggerFactory.getLogger(BuilderSink.class.getName());

    private final String builderName;
    private final LaunchConfiguration lauchConfig;
    private final ILanguageIdentifierService languageIdentifierService;

    public BuilderSink(String builderName, LaunchConfiguration launchConfig,
        ILanguageIdentifierService languageIdentifierService) {
        this.builderName = builderName;
        this.lauchConfig = launchConfig;
        this.languageIdentifierService = languageIdentifierService;
        logger.trace("Created new builder for {}", builderName);
    }

    /**
     * Call a builder on the file's source or analyzed AST. The builder is expected to have the following format:
     * 
     * <code>
     * builder:
     * 	(node, position, ast, path, project-path) -> (filename, result)
     * </code>
     * 
     * or
     * 
     * <code>
     * builder:
     * 	(node, position, ast, path, project-path) -> None()
     * </code>
     * 
     * In the latter option the assumption being that the builder code itself is taking care of writing files to disk if
     * necessary.
     * 
     * NB: The current implementation calls the builder on the following input:
     * 
     * <code>
     * 	(ast, [], ast, path, project-path)
     * </code>
     * 
     * NB: The current implementation assumes that the result is a StrategoString and will not work with a term.
     * 
     * @param product
     *            The {@link BuilderInputTerm} to run this builder on.
     * 
     * @throws MetaborgRuntimeException
     */
    @Override public void sink(Diff<BuilderInputTerm> product) {
        final FileObject file = product.getPayload().getFile();
        final ILanguageImpl language = languageIdentifierService.identify(file);
        final StrategoTransformAction action;
        try {
            action = (StrategoTransformAction) ServiceRegistry.INSTANCE().getService(MenuService.class)
                .action(language, builderName);
        } catch(MetaborgException e) {
            throw new MetaborgRuntimeException("Builder could not be found", e);
        }

        if(action == null) {
            logger.error("Builder {} could not be found", builderName);
        }
        logger.debug("Invoking builder {} on file {}", action.name, file);
        try {
            IStrategoTerm inputTuple = product.getPayload().toStratego();
            assert inputTuple != null && inputTuple.getSubtermCount() == 5;
            invoke(action, inputTuple);
        } catch(FileSystemException e) {
            final String msg = "Cannot construct input tuple for builder";
            logger.error(msg, e);
            throw new MetaborgRuntimeException(msg, e);
        }
    }

    private IStrategoTerm invoke(StrategoTransformAction action, IStrategoTerm input) {
        final ServiceRegistry services = ServiceRegistry.INSTANCE();
        final IStrategoRuntimeService runtimeService = services.getService(IStrategoRuntimeService.class);
        final ILanguageService languageService = services.getService(ILanguageService.class);
        final ILanguageImpl languageImpl = languageService.getImpl(action.inputLanguageId);
        final ILanguageComponent component = Iterables.get(languageImpl.components(), 0);
        final IStrategoTerm result;
        try {
            final HybridInterpreter interpreter =
                runtimeService.runtime(component,
                    new SpoofaxContext(ServiceRegistry.INSTANCE().getService(ResourceService.class),
                        new ContextIdentifier(lauchConfig.projectDir, languageImpl), ServiceRegistry.INSTANCE()
                            .injector()));
            result = StrategoRuntimeUtils.invoke(interpreter, input, action.strategy);
        } catch(MetaborgException e) {
            final String msg = "Cannot get Stratego interpreter, or Stratego invocation failed";
            logger.error(msg, e);
            throw new MetaborgRuntimeException(msg, e);
        }
        processResult(action, result);
        return result;
    }

    private void processResult(StrategoTransformAction action, IStrategoTerm result) {
        if(isWriteFile(result)) {
            try {
                final FileObject resultFile =
                    lauchConfig.projectDir.resolveFile(((IStrategoString) result.getSubterm(0)).stringValue());
                final IStrategoTerm resultTerm = result.getSubterm(1);
                final String resultContents;
                if(resultTerm.getTermType() == IStrategoTerm.STRING) {
                    resultContents = ((IStrategoString) resultTerm).stringValue();
                } else {
                    resultContents = resultTerm.toString();
                }

                try(OutputStream stream = resultFile.getContent().getOutputStream()) {
                    IOUtils.write(resultContents, stream);
                }
            } catch(IOException e) {
                throw new MetaborgRuntimeException("Builder " + action.name + "failed to write result", e);
            }
        }
    }

    private boolean isWriteFile(final IStrategoTerm result) {
        if(result instanceof IStrategoAppl) {
            if(((IStrategoAppl) result).getName().equals("None")) {
                return false;
            } else {
                logger.error("Builder returned an unsupported result type {}", result);
                throw new MetaborgRuntimeException("Unsupported return value from builder: " + result);
            }
        } else {
            if(result == null || result.getSubtermCount() != 2 || !(result.getSubterm(0) instanceof IStrategoString)) {
                logger.error("Builder returned an unsupported result type {}", result);
                throw new MetaborgRuntimeException("Unsupported return value from builder: " + result);
            } else {
                return true;
            }
        }
    }
}
