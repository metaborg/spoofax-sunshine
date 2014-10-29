/**
 * 
 */
package org.metaborg.sunshine.services.analyzer;

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metaborg.spoofax.core.parser.ParseResult;
import org.metaborg.sunshine.environment.ServiceRegistry;
import org.metaborg.sunshine.pipeline.connectors.ALinkManyToMany;
import org.metaborg.sunshine.pipeline.diff.Diff;
import org.metaborg.sunshine.pipeline.diff.DiffKind;
import org.metaborg.sunshine.pipeline.diff.MultiDiff;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class AnalyzerLink extends
		ALinkManyToMany<ParseResult<IStrategoTerm>, AnalysisFileResult> {

	private static final Logger logger = LogManager
			.getLogger(AnalyzerLink.class.getName());

	@Override
	public MultiDiff<AnalysisFileResult> sinkWork(
			MultiDiff<ParseResult<IStrategoTerm>> input) {
		logger.debug("Analyzing {} changed files", input.size());

		final Collection<AnalysisResult> aResults = ServiceRegistry.INSTANCE()
				.getService(AnalysisService.class).analyze(input.values());

		logger.trace("Analysis completed with {} results", aResults.size());
		final MultiDiff<AnalysisFileResult> results = new MultiDiff<AnalysisFileResult>();
		for (AnalysisResult res : aResults) {
			for (AnalysisFileResult fileResult : res.fileResults) {
				// TODO this may be wrong because not everything is an ADDITION
				results.add(new Diff<AnalysisFileResult>(fileResult,
						DiffKind.ADDITION));
			}
		}
		return results;
	}

}
