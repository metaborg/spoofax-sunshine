/**
 * 
 */
package org.spoofax.sunshine.pipeline.services;

import java.io.File;
import java.util.Collection;

import org.spoofax.sunshine.parser.model.IStrategoParseOrAnalyzeResult;
import org.spoofax.sunshine.pipeline.connectors.ALinkManyToMany;
import org.spoofax.sunshine.pipeline.diff.Diff;
import org.spoofax.sunshine.pipeline.diff.DiffKind;
import org.spoofax.sunshine.pipeline.diff.MultiDiff;
import org.spoofax.sunshine.services.analysis.AnalysisService;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class AnalyzerLink extends
	ALinkManyToMany<File, IStrategoParseOrAnalyzeResult> {

    @Override
    public MultiDiff<IStrategoParseOrAnalyzeResult> sinkWork(
	    MultiDiff<File> input) {
	System.err.println("Analyzing " + input.size() + " files ");
	final Collection<IStrategoParseOrAnalyzeResult> aResults = AnalysisService
		.INSTANCE()
		.analyze(input.values());
	final MultiDiff<IStrategoParseOrAnalyzeResult> results = new MultiDiff<IStrategoParseOrAnalyzeResult>();
	for (IStrategoParseOrAnalyzeResult res : aResults) {
	    results.add(new Diff<IStrategoParseOrAnalyzeResult>(res,
		    DiffKind.ADDITION));
	}
	return results;
    }

}
