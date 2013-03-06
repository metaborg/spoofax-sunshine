/**
 * 
 */
package org.spoofax.sunshine.framework.language;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import org.spoofax.sunshine.parser.framework.FileBasedParseTableProvider;
import org.spoofax.sunshine.parser.framework.IParseTableProvider;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class AdHocJarBasedLanguage extends ALanguage {

	
	public final String[] extens;
	public final String startSymbol;
	public final FileBasedParseTableProvider parseTableProvider;
	public final String analysisFunction;
	public final File[] jarfiles;

	public AdHocJarBasedLanguage(String name, String[] extens, String startSymbol, File parseTable, String analysisFunction,
			File jarfile) {
		super(name, LanguageNature.JAR_NATURE);

		assert name != null && name.length() > 0;
		assert extens != null && extens.length > 0;
		assert startSymbol != null && startSymbol.length() > 0;
		assert parseTable != null;
		assert analysisFunction != null && analysisFunction.length() > 0;
		assert jarfile != null && jarfile.getName().endsWith(".jar");
		
		this.extens = new String[extens.length];
		System.arraycopy(extens, 0, this.extens, 0, extens.length);
		this.startSymbol = startSymbol;
		this.parseTableProvider = new FileBasedParseTableProvider(parseTable);
		this.analysisFunction = analysisFunction;
		this.jarfiles = new File[] { jarfile };
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
	public IParseTableProvider getParseTableProvider() {
		return this.parseTableProvider;
	}

	@Override
	public String getAnalysisFunction() {
		return analysisFunction;
	}

	@Override
	public File[] getCompilerFiles() {
		return this.jarfiles;
	}

}
