package org.metaborg.sunshine.services.parser;

import org.metaborg.core.analysis.AnalysisFileResult;
import org.metaborg.core.syntax.ParseResult;
import org.metaborg.sunshine.pipeline.connectors.ALinkManyToMany;
import org.metaborg.sunshine.pipeline.diff.Diff;
import org.metaborg.sunshine.pipeline.diff.MultiDiff;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class ParseToAnalysisResultLink extends
    ALinkManyToMany<ParseResult<IStrategoTerm>, AnalysisFileResult<IStrategoTerm, IStrategoTerm>> {
    @Override public MultiDiff<AnalysisFileResult<IStrategoTerm, IStrategoTerm>> sinkWork(
        MultiDiff<ParseResult<IStrategoTerm>> input) {
        final MultiDiff<AnalysisFileResult<IStrategoTerm, IStrategoTerm>> output =
            new MultiDiff<AnalysisFileResult<IStrategoTerm, IStrategoTerm>>();
        for(Diff<ParseResult<IStrategoTerm>> diff : input) {
            final ParseResult<IStrategoTerm> parseResult = diff.getPayload();
            output.add(new Diff<AnalysisFileResult<IStrategoTerm, IStrategoTerm>>(
                new AnalysisFileResult<IStrategoTerm, IStrategoTerm>(parseResult.result, parseResult.source, null,
                    parseResult.messages, parseResult), diff.getDiffKind()));
        }
        return output;
    }
}
