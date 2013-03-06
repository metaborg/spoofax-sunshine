/**
 * 
 */
package org.spoofax.sunshine.framework.language;

import java.io.File;
import java.util.Collection;

import org.spoofax.sunshine.parser.framework.IParseTableProvider;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public abstract class ALanguage {

	protected final String name;
	protected final LanguageNature nature;

	protected ALanguage(String name, LanguageNature nature) {
		assert name != null && name.length() > 0;
		assert nature != null;
		this.name = name;
		this.nature = nature;
	}

	public String getName() {
		return this.name;
	}
	
	public LanguageNature getNature() {
		return nature;
	}
	
	public abstract Collection<String> getFileExtensions();

	public abstract String getStartSymbol();

	public abstract IParseTableProvider getParseTableProvider();

	public abstract File[] getCompilerFiles();

	public abstract String getAnalysisFunction();

}
