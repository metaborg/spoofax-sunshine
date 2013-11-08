package org.metaborg.sunshine.prims;

import org.metaborg.sunshine.Environment;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * 
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class ProjectPathPrimitive extends AbstractPrimitive {

	public ProjectPathPrimitive() {
		super("SSL_EXT_projectpath", 0, 0);
	}

	@Override
	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars)
			throws InterpreterException {

		final Environment sunshineEnv = Environment.INSTANCE();

		final IStrategoString projectPath = sunshineEnv.termFactory
				.makeString(sunshineEnv.projectDir.getAbsolutePath());
		env.setCurrent(projectPath);
		return true;
	}

}
