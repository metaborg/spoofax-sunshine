/**
 * 
 */
package org.spoofax.sunshine.parser.framework;

import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public interface IParser {

	IStrategoTerm parse(String input, String filename) throws ParserException;
	IParserConfig getConfig();
}
