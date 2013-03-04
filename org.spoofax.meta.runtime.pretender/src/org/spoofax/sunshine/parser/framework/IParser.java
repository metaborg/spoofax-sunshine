/**
 * 
 */
package org.spoofax.sunshine.parser.framework;

import java.io.InputStream;

import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public interface IParser {

	IParserConfig getConfiguration();
	void setConfiguration(IParserConfig config);
	
	IStrategoTerm parse(String input, String filename) throws ParserException;

	IStrategoTerm parse(InputStream input, String filename) throws ParserException;

}
