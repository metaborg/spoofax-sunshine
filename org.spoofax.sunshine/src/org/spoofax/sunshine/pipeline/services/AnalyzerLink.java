/**
 * 
 */
package org.spoofax.sunshine.pipeline.services;

import java.io.File;
import java.util.Collection;

import org.spoofax.sunshine.pipeline.connectors.ALinkManyToMany;
import org.spoofax.sunshine.pipeline.diff.Diff;
import org.spoofax.sunshine.pipeline.diff.DiffKind;
import org.spoofax.sunshine.pipeline.diff.MultiDiff;
import org.spoofax.sunshine.services.analysis.AnalysisService;
import org.spoofax.sunshine.services.analysis.IAnalysisResult;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class AnalyzerLink extends ALinkManyToMany<File, IAnalysisResult> {

    @Override
    public MultiDiff<IAnalysisResult> sinkWork(MultiDiff<File> input) {
	final Collection<IAnalysisResult> aResults = AnalysisService.INSTANCE()
		.analyze(input.values());
	final MultiDiff<IAnalysisResult> results = new MultiDiff<IAnalysisResult>();
	for (IAnalysisResult res : aResults) {
	    results.add(new Diff<IAnalysisResult>(res, DiffKind.MODIFICATION));
	}
	return results;
    }

}
