/**
 * 
 */
package org.spoofax.sunshine.model.language;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import org.spoofax.sunshine.drivers.SunshineLanguageArguments;
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

	public static Language fromArguments(SunshineLanguageArguments args) {
		String[] extens = args.extens.toArray(new String[args.extens.size()]);

		int numJars = args.jars.size();
		int numCtrees = args.ctrees.size();
		File[] compilerFiles = new File[numJars + numCtrees];
		for (int i = 0; i < (numJars + numCtrees); i++) {
			if (i < numJars)
				compilerFiles[i] = new File(args.jars.get(i));
			else
				compilerFiles[i] = new File(args.ctrees.get(i - numJars));
		}

		return new Language(args.lang, extens, args.ssymb, new File(args.tbl), args.observer,
				compilerFiles);
	}

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
