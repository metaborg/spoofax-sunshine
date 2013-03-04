/**
 * 
 */
package org.spoofax.sunshine.parser.framework;

import java.io.File;

import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public interface IParseController {
	File getFile();
	IStrategoTerm getCurrentAst();
	IStrategoTerm parse();
}
