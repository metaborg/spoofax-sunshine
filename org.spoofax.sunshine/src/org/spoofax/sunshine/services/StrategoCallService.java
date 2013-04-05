/**
 * 
 */
package org.spoofax.sunshine.services;

import org.spoofax.interpreter.core.InterpreterErrorExit;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.core.InterpreterExit;
import org.spoofax.interpreter.core.UndefinedStrategyException;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.sunshine.CompilerException;
import org.spoofax.sunshine.model.language.ALanguage;
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

	public IStrategoTerm callStratego(ALanguage lang, String strategy, IStrategoTerm input) throws CompilerException {
		assert lang != null;
		assert strategy != null && strategy.length() > 0;
		assert input != null;

		final HybridInterpreter runtime = RuntimeService.INSTANCE().getRuntime(lang);

		runtime.setCurrent(input);
		boolean success = false;
		try {
			success = runtime.invoke(strategy);
		} catch (InterpreterErrorExit e) {
			throw new CompilerException("Stratego call failed", e);
		} catch (InterpreterExit e) {
			throw new CompilerException("Stratego call failed", e);
		} catch (UndefinedStrategyException e) {
			throw new CompilerException("Stratego call failed", e);
		} catch (InterpreterException e) {
			throw new CompilerException("Stratego call failed", e);
		}
		
		if (success) {
			return runtime.current();
		}else{
			throw new CompilerException("Stratego call failed w/o exception");
		}

	}

}
