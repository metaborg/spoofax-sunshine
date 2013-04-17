package org.spoofax.sunshine.prims;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * 
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class CompleteWorkUnitPrimitive extends AbstractPrimitive {

    public CompleteWorkUnitPrimitive() {
	super("SSL_EXT_complete_work_unit", 0, 0);
    }

    @Override
    public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars)
	    throws InterpreterException {

	return true;
    }

}
