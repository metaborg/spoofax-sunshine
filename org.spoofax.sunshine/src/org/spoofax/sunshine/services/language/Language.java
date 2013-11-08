/**
 * 
 */
package org.spoofax.sunshine.services.language;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import org.spoofax.sunshine.parser.model.IParseTableProvider;
import org.spoofax.sunshine.services.parser.FileBasedParseTableProvider;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class Language extends ALanguage {

	public final String[] extens;
	public final String startSymbol;
	public final FileBasedParseTableProvider parseTableProvider;
	public final String analysisFunction;
	public final File[] compilerFiles;

	public Language(String name, String[] extens, String startSymbol, File parseTable,
			String analysisFunction, File[] compilerFiles) {
		super(name);

		assert name != null && name.length() > 0;
		assert extens != null && extens.length > 0;
		assert startSymbol != null && startSymbol.length() > 0;
		assert parseTable != null;
		assert analysisFunction != null && analysisFunction.length() > 0;
		assert compilerFiles != null && compilerFiles.length > 0;

		this.extens = extens;
		this.startSymbol = startSymbol;
		this.parseTableProvider = new FileBasedParseTableProvider(parseTable);
		this.analysisFunction = analysisFunction;
		this.compilerFiles = compilerFiles;
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
		return this.compilerFiles;
	}

}
