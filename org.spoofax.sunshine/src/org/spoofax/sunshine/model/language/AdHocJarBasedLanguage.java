/**
 * 
 */
package org.spoofax.sunshine.model.language;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import org.spoofax.sunshine.parser.impl.FileBasedParseTableProvider;
import org.spoofax.sunshine.parser.model.IParseTableProvider;

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
    public String overrideAnalysisFunction;

    public AdHocJarBasedLanguage(String name, String[] extens,
	    String startSymbol, File parseTable, String analysisFunction,
	    File[] jars) {
	super(name, LanguageNature.JAR_NATURE);

	assert name != null && name.length() > 0;
	assert extens != null && extens.length > 0;
	assert startSymbol != null && startSymbol.length() > 0;
	assert parseTable != null;
	assert analysisFunction != null && analysisFunction.length() > 0;
	assert jars != null && jars.length > 0;

	this.extens = new String[extens.length];
	System.arraycopy(extens, 0, this.extens, 0, extens.length);
	this.startSymbol = startSymbol;
	this.parseTableProvider = new FileBasedParseTableProvider(parseTable);
	this.analysisFunction = analysisFunction;
	this.jarfiles = new File[jars.length];
	System.arraycopy(jars, 0, this.jarfiles, 0, jars.length);
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
	if (overrideAnalysisFunction != null) {
	    return overrideAnalysisFunction;
	}
	return analysisFunction;
    }

    @Override
    public File[] getCompilerFiles() {
	return this.jarfiles;
    }

    @Override
    public void overrideAnalysisFunction(String newFunction) {
	overrideAnalysisFunction = newFunction;
    }

    @Override
    public void restoreAnalysisFunction() {
	overrideAnalysisFunction = null;
    }

}
