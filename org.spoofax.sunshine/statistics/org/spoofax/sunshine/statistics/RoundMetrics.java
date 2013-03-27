/**
 * 
 */
package org.spoofax.sunshine.statistics;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.sunshine.framework.messages.IAnalysisResult;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class RoundMetrics {

	/*
	 * CORRECTNESS:
	 * 
	 * + FULL:
	 * 
	 * - all IAnalysisResults (asts, errors, warnings, notes, eval-tasks, error-tasks)
	 * 
	 * - index
	 * 
	 * + INCREMENTAL:
	 * 
	 * - all IAnalysisResults => only for modified or dependent files => all others are assumed
	 * unchanged
	 * 
	 * - index
	 * 
	 * PERFORMANCE:
	 * 
	 * - total analysis time
	 * 
	 * - parsing time
	 * 
	 * - per analysis phase time (3)
	 * 
	 * - task evaluation time
	 * 
	 * - index commit time
	 * 
	 * METRICS:
	 * 
	 * - project LOC, TDEF, TCAL
	 * 
	 * - commit affected lines of code
	 */
	
	public final RoundType roundType;
	public IStrategoTerm index;
	public final Map<File, IAnalysisResult> analysisResults;
	public IStrategoList tasks;
	public long totalTime, parseTime, collectTime, evalTime, dependTime, commitTime;
	public int commitDeltaLines;

	public RoundMetrics(RoundType roundTy) {
		assert roundTy != null;
		roundType = roundTy;
		analysisResults = new HashMap<File, IAnalysisResult>();
	}

	public enum RoundType {
		FULL, INCREMENTAL
	}

}
