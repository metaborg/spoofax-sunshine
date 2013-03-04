/**
 * 
 */
package org.spoofax.sunshine.framework.services;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.sunshine.parser.framework.IParseController;
import org.spoofax.sunshine.parser.impl.JSGLRParseController;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 */
public class ParseService {
	private static ParseService INSTANCE;

	private final Map<File, IParseController> controllers = new HashMap<File, IParseController>();

	private ParseService() {
	}

	public static final ParseService INSTANCE() {
		if (INSTANCE == null) {
			INSTANCE = new ParseService();
		}
		return INSTANCE;
	}

	public IStrategoTerm parse(File f) {
		return getParseController(f).parse();
	}

	private IParseController getParseController(File f) {
		IParseController controller = controllers.get(f);
		if (controller == null) {
			controller = new JSGLRParseController(f);
			controllers.put(f, controller);
		}
		return controller;
	}
}
