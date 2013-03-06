/**
 * 
 */
package org.spoofax.sunshine.parser.framework;


/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 *
 */
public interface IParserConfig {
	String getStartSymbol();
	IParseTableProvider getParseTableProvider();
	int getTimeout();
}
