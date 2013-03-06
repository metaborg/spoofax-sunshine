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
 * Singleton service for access to {@link IParseController} on a per-file basis. A mapping between
 * {@link File} and {@link IParseController} is maintained.
 * 
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

	/**
	 * Convenience method for parsing a file. Calls are dispatched to
	 * {@link IParseController#parse()}. On call of this method the corresponding
	 * {@link IParseController} for the given file is retrieved and a call to its
	 * {@link IParseController#parse()} method is made, returning the result.
	 * 
	 * If an {@link IParseController} corresponding to the given file does not exist yet then one is
	 * first created.
	 * 
	 * @see IParseController#parse()
	 * @param file
	 *            The {@link File} to be parsed.
	 * @return The {@link IStrategoTerm} representing the contents of the file is parsing was
	 *         successful, <code>null</code> otherwise.
	 */
	public IStrategoTerm parse(File file) {
		IParseController controller = controllers.get(file);
		if (controller == null) {
			controller = new JSGLRParseController(file);
			controllers.put(file, controller);
		}
		return controller.getCurrentAst();
	}
}
