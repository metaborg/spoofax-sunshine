/**
 * 
 */
package org.spoofax.sunshine.framework.language;

import java.io.File;
import java.util.Collection;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 *
 */
public interface ILanguage {
	String getName();
	Collection<String> getFileExtensions();
	String getStartSymbol();
	File getParseTable();
}
