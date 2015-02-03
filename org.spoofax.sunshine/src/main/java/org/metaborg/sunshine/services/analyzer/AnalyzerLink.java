package org.metaborg.sunshine.services.analyzer;

import java.util.Collection;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.SpoofaxException;
import org.metaborg.spoofax.core.analysis.AnalysisFileResult;
import org.metaborg.spoofax.core.analysis.AnalysisResult;
import org.metaborg.spoofax.core.analysis.IAnalysisService;
import org.metaborg.spoofax.core.context.SpoofaxContext;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.syntax.ParseResult;
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

        final Collection<AnalysisResult<IStrategoTerm, IStrategoTerm>> aResults =
            analyze(
                input.values(),
                ServiceRegistry.INSTANCE().getService(
                    new TypeLiteral<IAnalysisService<IStrategoTerm, IStrategoTerm>>() {}));

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
        throws SpoofaxException {
        final FileObject projectDir = ServiceRegistry.INSTANCE().getService(LaunchConfiguration.class).projectDir;
        logger.debug("Analyzing {} files", inputs.size());
        Multimap<ILanguage, ParseResult<IStrategoTerm>> lang2files = LinkedHashMultimap.create();
        for(ParseResult<IStrategoTerm> input : inputs) {
            lang2files.put(input.parsedWith, input);
        }
        logger.trace("Files grouped in {} languages", lang2files.size());
        final Collection<AnalysisResult<IStrategoTerm, IStrategoTerm>> results =
            Lists.newArrayList(lang2files.keySet().size());
        for(ILanguage lang : lang2files.keySet()) {
            results.add(analyzer.analyze(lang2files.get(lang), new SpoofaxContext(lang, projectDir)));
        }
        return results;
    }
}
