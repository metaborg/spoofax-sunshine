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
@Deprecated
public class SetMarkersPrimitive extends AbstractPrimitive {

    public SetMarkersPrimitive() {
	super("SSL_EXT_set_markers", 0, 1);
    }

    @Override
    public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars)
	    throws InterpreterException {

	// IStrategoTerm previousTerm = env.current();
	// AnalysisService.INSTANCE().storeResults(SourceAttachment.getResource(tvars[0]),
	// env.current());
	// env.setCurrent(previousTerm);
	//
	// return true;
	// System.err.println("WARNING: call to set markers primitive. doing nothing!");
	return true;
    }

}
