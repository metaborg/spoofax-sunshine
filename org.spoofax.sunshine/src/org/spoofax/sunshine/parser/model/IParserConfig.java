/**
 * 
 */
package org.spoofax.sunshine.parser.model;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public interface IParserConfig {
	String getStartSymbol();

	IParseTableProvider getParseTableProvider();

	int getTimeout();
}
