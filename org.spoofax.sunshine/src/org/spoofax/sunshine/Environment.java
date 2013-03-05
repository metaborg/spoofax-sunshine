/**
 * 
 */
package org.spoofax.sunshine;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.io.ParseTableManager;
import org.spoofax.terms.TermFactory;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class Environment {

	public final ITermFactory termFactory;
	public final ParseTableManager parseTableMgr;

	private Environment() {
		this.termFactory = new TermFactory().getFactoryWithStorageType(IStrategoTerm.MUTABLE);
		this.parseTableMgr = new ParseTableManager(termFactory);
	}

	private static Environment INSTANCE;

	public static final Environment INSTANCE() {
		if (INSTANCE == null) {
			INSTANCE = new Environment();
		}
		return INSTANCE;
	}

}
