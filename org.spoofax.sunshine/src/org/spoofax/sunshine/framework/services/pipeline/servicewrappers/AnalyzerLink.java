/**
 * 
 */
package org.spoofax.sunshine.framework.services.pipeline.servicewrappers;

import java.io.File;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.sunshine.framework.services.AnalysisService;
import org.spoofax.sunshine.framework.services.pipeline.ALinkManyToManySequential;
import org.spoofax.sunshine.framework.services.pipeline.diff.MultiDiff;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 *
 */
public class AnalyzerLink extends
	ALinkManyToManySequential<File, IStrategoTerm> {

    @Override
    public MultiDiff<IStrategoTerm> sinkWork(MultiDiff<File> input) {
	// TODO
	AnalysisService.INSTANCE().analyze(input.values());
	return null;
    }

}
