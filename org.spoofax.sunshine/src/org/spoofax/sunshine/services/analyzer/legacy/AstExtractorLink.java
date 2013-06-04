package org.spoofax.sunshine.services.analyzer.legacy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.sunshine.parser.model.IStrategoParseOrAnalyzeResult;
import org.spoofax.sunshine.pipeline.connectors.ALinkManyToMany;
import org.spoofax.sunshine.pipeline.diff.Diff;
import org.spoofax.sunshine.pipeline.diff.DiffKind;
import org.spoofax.sunshine.pipeline.diff.MultiDiff;

public class AstExtractorLink extends ALinkManyToMany<IStrategoParseOrAnalyzeResult, IStrategoTerm> {

	private static final Logger logger = LogManager.getLogger(AstExtractorLink.class.getName());

	@Override
	public MultiDiff<IStrategoTerm> sinkWork(MultiDiff<IStrategoParseOrAnalyzeResult> input) {
		logger.trace("Selecting ASTs from {} inputs", input.size());
		MultiDiff<IStrategoTerm> result = new MultiDiff<IStrategoTerm>();

		for (Diff<IStrategoParseOrAnalyzeResult> diff : input) {
			DiffKind kind = diff.getDiffKind();
			if (diff.getPayload().ast() != null) {
				result.add(new Diff<IStrategoTerm>(diff.getPayload().ast(), kind));
			}
		}
		logger.trace("ASTs selected");
		return result;
	}

}
