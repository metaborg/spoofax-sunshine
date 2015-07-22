package org.metaborg.sunshine.services.analyzer.legacy;

import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.analysis.AnalysisFileResult;
import org.metaborg.core.context.ContextIdentifier;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.ILanguageIdentifierService;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageSeverity;
import org.metaborg.core.resource.ResourceService;
import org.metaborg.core.syntax.ParseResult;
import org.metaborg.spoofax.core.analysis.StrategoAnalysisService;
import org.metaborg.spoofax.core.context.SpoofaxContext;
import org.metaborg.spoofax.core.stratego.StrategoFacet;
import org.metaborg.spoofax.core.stratego.StrategoRuntimeService;
import org.metaborg.sunshine.environment.LaunchConfiguration;
import org.metaborg.sunshine.environment.ServiceRegistry;
import org.metaborg.sunshine.pipeline.connectors.ALinkOneToOne;
import org.metaborg.sunshine.pipeline.diff.Diff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.HybridInterpreter;

import com.google.inject.Inject;

public class LegacyAnalyzerLink extends
    ALinkOneToOne<ParseResult<IStrategoTerm>, AnalysisFileResult<IStrategoTerm, IStrategoTerm>> {

    private final static String ANALYSIS_CRASHED_MSG = "Analysis failed";

    private static final Logger logger = LoggerFactory.getLogger(LegacyAnalyzerLink.class.getName());

    private final ILanguageIdentifierService languageIdentifierService;

    @Inject public LegacyAnalyzerLink(ILanguageIdentifierService languageIdentifierService) {
        this.languageIdentifierService = languageIdentifierService;
    }

    @Override public Diff<AnalysisFileResult<IStrategoTerm, IStrategoTerm>> sinkWork(
        Diff<ParseResult<IStrategoTerm>> input) {
        return new Diff<AnalysisFileResult<IStrategoTerm, IStrategoTerm>>(analyze(input.getPayload()),
            input.getDiffKind());
    }

    private AnalysisFileResult<IStrategoTerm, IStrategoTerm> analyze(ParseResult<IStrategoTerm> parseResult) {
        logger.debug("Analyzing AST of file {}", parseResult.source);
        if(parseResult.result == null) {
            logger.info("Analysis cannot continue because there is no AST for file {}", parseResult.source);
            return null;
        }
        ServiceRegistry serviceRegistry = ServiceRegistry.INSTANCE();
        final FileObject file = parseResult.source;
        ILanguageImpl lang = languageIdentifierService.identify(file);

        LaunchConfiguration launch = serviceRegistry.getService(LaunchConfiguration.class);
        ITermFactory termFactory = launch.termFactory;

        final HybridInterpreter runtime;
        final IStrategoString fileTerm;
        final IStrategoString projectTerm;
        try {
            runtime =
                serviceRegistry.getService(StrategoRuntimeService.class).runtime(
                    new SpoofaxContext(ServiceRegistry.INSTANCE().getService(ResourceService.class),
                        new ContextIdentifier(launch.projectDir, lang), serviceRegistry.injector()));
            fileTerm =
                termFactory.makeString(launch.projectDir.getName().getRelativeName(parseResult.source.getName()));
            projectTerm = termFactory.makeString(launch.projectDir.getName().getPath());
        } catch(MetaborgException e) {
            final String msg = "Cannot get Stratego interpreter";
            logger.error(msg, e);
            throw new MetaborgRuntimeException(msg, e);
        } catch(FileSystemException e) {
            final String msg = "Cannot create path and project-path for analysis input";
            logger.error(msg, e);
            throw new MetaborgRuntimeException(msg, e);
        }

        IStrategoTuple inputTerm = termFactory.makeTuple(parseResult.result, fileTerm, projectTerm);
        runtime.setCurrent(inputTerm);
        String function = lang.facets(StrategoFacet.class).analysisStrategy();
        boolean success;
        try {
            success = runtime.invoke(function);
            if(!success) {
                throw new MetaborgRuntimeException(ANALYSIS_CRASHED_MSG);
            } else {
                logger.debug("Ignoring further files to analyze. Not implemented");
                IStrategoTuple resultTuple = (IStrategoTuple) runtime.current();
                logger.trace("Analysis resulted in a {} tuple", resultTuple.getSubtermCount());
                return makeAnalysisResult(parseResult, resultTuple);
            }
        } catch(InterpreterException e) {
            throw new MetaborgRuntimeException(ANALYSIS_CRASHED_MSG, e);
        }
    }

    private AnalysisFileResult<IStrategoTerm, IStrategoTerm> makeAnalysisResult(ParseResult<IStrategoTerm> parseResult,
        IStrategoTuple resultTuple) {
        assert resultTuple != null;
        assert resultTuple.getSubtermCount() == 5;
        IStrategoTerm ast = resultTuple.getSubterm(0);
        FileObject file = parseResult.source;
        Collection<IMessage> messages = new HashSet<IMessage>();
        messages.addAll(StrategoAnalysisService.makeMessages(file, MessageSeverity.ERROR,
            (IStrategoList) resultTuple.getSubterm(1)));
        messages.addAll(StrategoAnalysisService.makeMessages(file, MessageSeverity.WARNING,
            (IStrategoList) resultTuple.getSubterm(2)));
        messages.addAll(StrategoAnalysisService.makeMessages(file, MessageSeverity.NOTE,
            (IStrategoList) resultTuple.getSubterm(3)));
        return new AnalysisFileResult<IStrategoTerm, IStrategoTerm>(ast, file, null, messages, parseResult);
    }
}
