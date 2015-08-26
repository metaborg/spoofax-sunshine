package org.metaborg.sunshine.services.analyzer;

import java.util.Collection;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.analysis.AnalysisException;
import org.metaborg.core.analysis.AnalysisFileResult;
import org.metaborg.core.analysis.AnalysisResult;
import org.metaborg.core.analysis.IAnalysisService;
import org.metaborg.core.context.ContextIdentifier;
import org.metaborg.core.context.IContext;
import org.metaborg.core.context.IContextFactory;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.syntax.ParseResult;
import org.metaborg.sunshine.environment.LaunchConfiguration;
import org.metaborg.sunshine.environment.ServiceRegistry;
import org.metaborg.sunshine.pipeline.connectors.ALinkManyToMany;
import org.metaborg.sunshine.pipeline.diff.Diff;
import org.metaborg.sunshine.pipeline.diff.DiffKind;
import org.metaborg.sunshine.pipeline.diff.MultiDiff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.TypeLiteral;

public class AnalyzerLink extends
    ALinkManyToMany<ParseResult<IStrategoTerm>, AnalysisFileResult<IStrategoTerm, IStrategoTerm>> {

    private static final Logger logger = LoggerFactory.getLogger(AnalyzerLink.class.getName());

    @Override public MultiDiff<AnalysisFileResult<IStrategoTerm, IStrategoTerm>> sinkWork(
        MultiDiff<ParseResult<IStrategoTerm>> input) {
        logger.debug("Analyzing {} changed files", input.size());

        Collection<AnalysisResult<IStrategoTerm, IStrategoTerm>> aResults;
        try {
            aResults =
                analyze(
                    input.values(),
                    ServiceRegistry.INSTANCE().getService(
                        new TypeLiteral<IAnalysisService<IStrategoTerm, IStrategoTerm>>() {}));
        } catch(AnalysisException e) {
            throw new MetaborgRuntimeException(e);
        }

        logger.trace("Analysis completed with {} results", aResults.size());
        final MultiDiff<AnalysisFileResult<IStrategoTerm, IStrategoTerm>> results =
            new MultiDiff<AnalysisFileResult<IStrategoTerm, IStrategoTerm>>();
        for(AnalysisResult<IStrategoTerm, IStrategoTerm> res : aResults) {
            for(AnalysisFileResult<IStrategoTerm, IStrategoTerm> fileResult : res.fileResults) {
                // TODO this may be wrong because not everything is an ADDITION
                results.add(new Diff<AnalysisFileResult<IStrategoTerm, IStrategoTerm>>(fileResult, DiffKind.ADDITION));
            }
        }
        return results;
    }

    public Collection<AnalysisResult<IStrategoTerm, IStrategoTerm>> analyze(
        Collection<ParseResult<IStrategoTerm>> inputs, IAnalysisService<IStrategoTerm, IStrategoTerm> analyzer)
        throws AnalysisException {
        final FileObject projectDir = ServiceRegistry.INSTANCE().getService(LaunchConfiguration.class).projectDir;
        logger.debug("Analyzing {} files", inputs.size());
        Multimap<ILanguageImpl, ParseResult<IStrategoTerm>> lang2files = LinkedHashMultimap.create();
        for(ParseResult<IStrategoTerm> input : inputs) {
            lang2files.put(input.language, input);
        }
        logger.trace("Files grouped in {} languages", lang2files.size());
        final Collection<AnalysisResult<IStrategoTerm, IStrategoTerm>> results =
            Lists.newArrayList(lang2files.keySet().size());
        for(ILanguageImpl lang : lang2files.keySet()) {
            final IContext context =
                ServiceRegistry.INSTANCE().getService(IContextFactory.class)
                    .create(new ContextIdentifier(projectDir, lang));
            results.add(analyzer.analyze(lang2files.get(lang), context));
        }
        return results;
    }
}
