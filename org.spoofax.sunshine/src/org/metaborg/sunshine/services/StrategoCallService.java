/**
 * 
 */
package org.metaborg.sunshine.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.sunshine.CompilerException;
import org.spoofax.interpreter.core.InterpreterErrorExit;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.core.InterpreterExit;
import org.spoofax.interpreter.core.UndefinedStrategyException;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.HybridInterpreter;

import com.google.inject.Inject;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class StrategoCallService {
	private static final Logger logger = LogManager
			.getLogger(StrategoCallService.class.getName());

	private final RuntimeService runtimeService;

	@Inject
	public StrategoCallService(RuntimeService runtimeService) {
		this.runtimeService = runtimeService;
	}

	public IStrategoTerm callStratego(ILanguage lang, String strategy,
			IStrategoTerm input) throws CompilerException {
		assert lang != null;
		assert strategy != null && strategy.length() > 0;
		assert input != null;
		logger.trace("Calling strategy {} with input {}", strategy, input);

		final HybridInterpreter runtime = runtimeService.getRuntime(lang);
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
		} else {
			throw new CompilerException("Stratego call failed w/o exception");
		}

	}

}
