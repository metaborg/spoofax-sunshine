/**
 * 
 */
package org.spoofax.sunshine.terms;

import org.spoofax.interpreter.core.Interpreter;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 *
 */
public class NewInputTermBuilder {
	
	private final Interpreter runtime;
	
	public NewInputTermBuilder(Interpreter interp) {
		assert interp != null;
		this.runtime = interp;
	}

}
