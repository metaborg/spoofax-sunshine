package org.metaborg.sunshine.services.parser;

import org.metaborg.spoofax.core.analysis.AnalysisFileResult;
import org.metaborg.spoofax.core.parser.ParseResult;
import org.metaborg.sunshine.pipeline.connectors.ALinkManyToMany;
import org.metaborg.sunshine.pipeline.diff.Diff;
import org.metaborg.sunshine.pipeline.diff.MultiDiff;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class ParseToAnalysisResultLink extends
		ALinkManyToMany<ParseResult<IStrategoTerm>, AnalysisFileResult> {
	@Override
	public MultiDiff<AnalysisFileResult> sinkWork(
			MultiDiff<ParseResult<IStrategoTerm>> input) {
		final MultiDiff<AnalysisFileResult> output = new MultiDiff<AnalysisFileResult>();
		for (Diff<ParseResult<IStrategoTerm>> diff : input) {
			final ParseResult<IStrategoTerm> parseResult = diff.getPayload();
			output.add(new Diff<AnalysisFileResult>(new AnalysisFileResult(
					parseResult, parseResult.source, parseResult.messages,
					parseResult.result), diff.getDiffKind()));
		}
		return output;
	}
}
