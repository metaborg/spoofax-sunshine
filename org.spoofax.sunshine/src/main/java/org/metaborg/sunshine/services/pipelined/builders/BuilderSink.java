package org.metaborg.sunshine.services.pipelined.builders;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.spoofax.core.SpoofaxException;
import org.metaborg.spoofax.core.context.SpoofaxContext;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ILanguageIdentifierService;
import org.metaborg.spoofax.core.service.actions.Action;
import org.metaborg.spoofax.core.service.actions.ActionsFacet;
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
     * @throws SpoofaxException
     */
    @Override public void sink(Diff<BuilderInputTerm> product) {
        final FileObject file = product.getPayload().getFile();
        final ILanguage language = languageIdentifierService.identify(file);
        final Action action = language.facet(ActionsFacet.class).get(builderName);

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
            throw new SpoofaxException(msg, e);
        }
    }

    private IStrategoTerm invoke(Action action, IStrategoTerm input) {
        final ServiceRegistry services = ServiceRegistry.INSTANCE();
        final LaunchConfiguration launch = services.getService(LaunchConfiguration.class);
        final IStrategoRuntimeService runtimeService = services.getService(IStrategoRuntimeService.class);
        final HybridInterpreter interpreter =
            runtimeService.runtime(new SpoofaxContext(action.inputLangauge, launch.projectDir));
        final IStrategoTerm result = StrategoRuntimeUtils.invoke(interpreter, input, action.strategoStrategy);
        processResult(action, result);
        return result;
    }

    private void processResult(Action action, IStrategoTerm result) {
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
                throw new SpoofaxException("Builder " + action.name + "failed to write result", e);
            }
        }
    }

    private boolean isWriteFile(final IStrategoTerm result) {
        if(result instanceof IStrategoAppl) {
            if(((IStrategoAppl) result).getName().equals("None")) {
                return false;
            } else {
                logger.error("Builder returned an unsupported result type {}", result);
                throw new SpoofaxException("Unsupported return value from builder: " + result);
            }
        } else {
            if(result == null || result.getSubtermCount() != 2 || !(result.getSubterm(0) instanceof IStrategoString)) {
                logger.error("Builder returned an unsupported result type {}", result);
                throw new SpoofaxException("Unsupported return value from builder: " + result);
            } else {
                return true;
            }
        }
    }
}
