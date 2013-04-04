/**
 * 
 */
package org.spoofax.sunshine.prims;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.sunshine.CompilerException;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
@Deprecated
public class QueueStrategyPrimitive extends AbstractPrimitive {

	private static final String NAME = "SSL_EXT_queue_strategy";

	QueueStrategyPrimitive() {
		super(NAME, 0, 2);
	}

	/**
	 * @see org.spoofax.interpreter.library.AbstractPrimitive#call(org.spoofax.interpreter.core.IContext,
	 *      org.spoofax.interpreter.stratego.Strategy[],
	 *      org.spoofax.interpreter.terms.IStrategoTerm[])
	 */
	@Override
	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) throws InterpreterException {
		
//		final String strategyName = ((IStrategoString) tvars[0]).stringValue();
//		final IStrategoTerm[] strfiles = (IStrategoTerm[]) ((IStrategoList) env.current()).getAllSubterms();
//		final Collection<File> files = new ArrayList<File>(strfiles.length); 
//		for (IStrategoTerm fn : strfiles) {
//			final StrategoString str = (StrategoString) fn;
//			files.add(new File(str.stringValue()));
//		}
//		
//		QueableAnalysisService.INSTANCE().enqueueAnalysis(files, strategyName);
//		
//		return true;
		
		System.err.println("WARNING: call to queue strategy primitive. doing nothing!");
		return true;
	}
	
}

