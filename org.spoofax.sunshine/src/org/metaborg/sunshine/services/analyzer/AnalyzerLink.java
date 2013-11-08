/**
 * 
 */
package org.metaborg.sunshine.services.analyzer;

import java.io.File;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metaborg.sunshine.pipeline.connectors.ALinkManyToMany;
import org.metaborg.sunshine.pipeline.diff.Diff;
import org.metaborg.sunshine.pipeline.diff.DiffKind;
import org.metaborg.sunshine.pipeline.diff.MultiDiff;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class AnalyzerLink extends ALinkManyToMany<File, AnalysisResult> {

	private static final Logger logger = LogManager.getLogger(AnalyzerLink.class.getName());

	@Override
	public MultiDiff<AnalysisResult> sinkWork(MultiDiff<File> input) {
		logger.debug("Analyzing {} changed files", input.size());

		final Collection<AnalysisResult> aResults = AnalysisService.INSTANCE()
				.analyze(input.values());

		logger.trace("Analysis completed with {} results", aResults.size());
		final MultiDiff<AnalysisResult> results = new MultiDiff<AnalysisResult>();
		for (AnalysisResult res : aResults) {
			// TODO this may be wrong because not everything is an ADDITION
			results.add(new Diff<AnalysisResult>(res, DiffKind.ADDITION));
		}
		return results;
	}

}
