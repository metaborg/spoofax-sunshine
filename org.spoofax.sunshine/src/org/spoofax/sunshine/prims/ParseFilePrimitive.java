/**
 * 
 */
package org.spoofax.sunshine.prims;

import java.io.File;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.sunshine.framework.services.ParseService;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class ParseFilePrimitive extends AbstractPrimitive {

	private static final String NAME = "SSL_EXT_parse_file";

	ParseFilePrimitive() {
		super(NAME, 0, 2);
	}

	/**
	 * @see org.spoofax.interpreter.library.AbstractPrimitive#call(org.spoofax.interpreter.core.IContext,
	 *      org.spoofax.interpreter.stratego.Strategy[],
	 *      org.spoofax.interpreter.terms.IStrategoTerm[])
	 */
	@Override
	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) throws InterpreterException {

		final IStrategoString file = (IStrategoString) env.current();
		final IStrategoTerm result = ParseService.INSTANCE().parse(new File(file.stringValue()));
		if (result != null) {
			env.setCurrent(result);
			return true;
		} else {
			return false;
		}
	}

}
