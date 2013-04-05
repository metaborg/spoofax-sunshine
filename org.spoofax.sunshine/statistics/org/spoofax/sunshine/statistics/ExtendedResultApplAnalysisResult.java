/**
 * 
 */
package org.spoofax.sunshine.statistics;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.sunshine.services.analysis.ResultApplAnalysisResult;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
@Deprecated
public class ExtendedResultApplAnalysisResult extends ResultApplAnalysisResult {

	private IStrategoList evaluatedTasks;
	private IStrategoList errorTasks;

	public ExtendedResultApplAnalysisResult(IStrategoAppl resultTerm) {
		super(resultTerm);
		init(resultTerm);
	}

	private void init(IStrategoAppl resultTerm) {
		assert resultTerm != null;
		this.evaluatedTasks = (IStrategoList) resultTerm.getSubterm(5);
		this.errorTasks = (IStrategoList) resultTerm.getSubterm(6);
	}
	
}
