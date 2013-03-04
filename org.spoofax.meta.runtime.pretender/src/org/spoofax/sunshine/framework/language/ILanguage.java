/**
 * 
 */
package org.spoofax.sunshine.framework.language;

import java.util.Collection;

import org.spoofax.sunshine.parser.framework.IParseTableProvider;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 *
 */
public interface ILanguage {
	String getName();
	Collection<String> getFileExtensions();
	String getStartSymbol();
	IParseTableProvider getParseTableProvider();
}
