/**
 * 
 */
package org.spoofax.sunshine.services.analyzer;

import java.io.File;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spoofax.sunshine.parser.model.IStrategoParseOrAnalyzeResult;
import org.spoofax.sunshine.pipeline.connectors.ALinkManyToMany;
import org.spoofax.sunshine.pipeline.diff.Diff;
import org.spoofax.sunshine.pipeline.diff.DiffKind;
import org.spoofax.sunshine.pipeline.diff.MultiDiff;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class AnalyzerLink extends
	ALinkManyToMany<File, IStrategoParseOrAnalyzeResult> {

    private static final Logger logger = LogManager
	    .getLogger(AnalyzerLink.class.getName());

    @Override
    public MultiDiff<IStrategoParseOrAnalyzeResult> sinkWork(
	    MultiDiff<File> input) {
	logger.debug("Analyzing {} changed files", input.size());

	final Collection<IStrategoParseOrAnalyzeResult> aResults = AnalysisService
		.INSTANCE().analyze(input.values());

	logger.trace("Analysis completed with {} results", aResults.size());
	final MultiDiff<IStrategoParseOrAnalyzeResult> results = new MultiDiff<IStrategoParseOrAnalyzeResult>();
	for (IStrategoParseOrAnalyzeResult res : aResults) {
	    // TODO this may be wrong because not everything is an ADDITION
	    results.add(new Diff<IStrategoParseOrAnalyzeResult>(res,
		    DiffKind.ADDITION));
	}
	return results;
    }

}
