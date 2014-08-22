/**
 * 
 */
package org.metaborg.sunshine.parser.model;

import org.metaborg.spoofax.core.parser.IParseTableProvider;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public interface IParserConfig {
	String getStartSymbol();

	IParseTableProvider getParseTableProvider();

	int getTimeout();
}
