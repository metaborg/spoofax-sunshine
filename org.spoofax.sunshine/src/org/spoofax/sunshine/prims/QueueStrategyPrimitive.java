/**
 * 
 */
package org.spoofax.sunshine.prims;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.library.ssl.SSLLibrary;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.sunshine.SunshineIOAgent;
import org.spoofax.sunshine.framework.services.RuntimeService;
import org.strategoxt.HybridInterpreter;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
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

		final String strategyName = ((IStrategoString) tvars[0]).stringValue();
		final SunshineIOAgent agent = (SunshineIOAgent) SSLLibrary.instance(env).getIOAgent();
		final HybridInterpreter runtime = RuntimeService.INSTANCE().getRuntime(agent.getLanguage());
		
		runtime.setCurrent(env.current());
		runtime.invoke(strategyName);
		runtime.uninit();

		return true;
	}

}
