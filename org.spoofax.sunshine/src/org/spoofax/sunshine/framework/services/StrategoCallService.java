/**
 * 
 */
package org.spoofax.sunshine.framework.services;

import org.spoofax.interpreter.core.InterpreterErrorExit;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.core.InterpreterExit;
import org.spoofax.interpreter.core.UndefinedStrategyException;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.sunshine.framework.language.ALanguage;
import org.strategoxt.HybridInterpreter;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class StrategoCallService {
	private static StrategoCallService INSTANCE;

	private StrategoCallService() {
	}

	public static StrategoCallService INSTANCE() {
		if (INSTANCE == null) {
			INSTANCE = new StrategoCallService();
		}
		return INSTANCE;
	}

	public IStrategoTerm callStratego(ALanguage lang, String strategy, IStrategoTerm input) throws InterpreterException {
		assert lang != null;
		assert strategy != null && strategy.length() > 0;
		assert input != null;

		final HybridInterpreter runtime = RuntimeService.INSTANCE().getRuntime(lang);

		runtime.setCurrent(input);
		try {
			boolean success = runtime.invoke(strategy);
			if (success) {
				return runtime.current();
			} else {
				return null;
			}
		} catch (InterpreterErrorExit e) {
			throw new InterpreterException(e.getMessage(), e);
		} catch (InterpreterExit e) {
			throw new InterpreterException(e.getMessage(), e);
		} catch (UndefinedStrategyException e) {
			throw new InterpreterException(e.getMessage(), e);
		}
		
	}

}
