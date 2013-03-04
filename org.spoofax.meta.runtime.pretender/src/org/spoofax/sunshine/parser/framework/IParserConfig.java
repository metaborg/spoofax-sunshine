/**
 * 
 */
package org.spoofax.sunshine.parser.framework;

import java.io.File;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 *
 */
public interface IParserConfig {
	String getStartSymbol();
	IParseTableProvider getParseTableProvider();
	int getTimeout();
}
