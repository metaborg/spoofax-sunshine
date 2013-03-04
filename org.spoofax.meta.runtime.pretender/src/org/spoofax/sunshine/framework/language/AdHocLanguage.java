/**
 * 
 */
package org.spoofax.sunshine.framework.language;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class AdHocLanguage implements ILanguage {

	public final String name;
	public final String[] extens;
	public final String startSymbol;
	public final File parseTable;

	public AdHocLanguage(String name, String[] extens, String startSymbol, File parseTable) {
		this.name = name;
		this.extens = new String[extens.length];
		System.arraycopy(extens, 0, this.extens, 0, extens.length);
		this.startSymbol = startSymbol;
		this.parseTable = parseTable;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Collection<String> getFileExtensions() {
		return Arrays.asList(extens);
	}

	@Override
	public String getStartSymbol() {
		return this.startSymbol;
	}

	@Override
	public File getParseTable() {
		return this.parseTable;
	}

}
