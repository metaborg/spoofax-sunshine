/**
 * 
 */
package org.metaborg.sunshine.services.language;

import java.io.File;
import java.util.Collection;

import org.metaborg.sunshine.parser.model.IParseTableProvider;
import org.metaborg.sunshine.services.pipelined.builders.IBuilder;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public abstract class ALanguage {

	protected final String name;

	protected ALanguage(String name) {
		assert name != null && name.length() > 0;
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public abstract Collection<String> getFileExtensions();

	public abstract String getStartSymbol();

	public abstract IParseTableProvider getParseTableProvider();

	public abstract File[] getCompilerFiles();

	public abstract String getAnalysisFunction();

	public abstract IBuilder getBuilder(String name);

	public abstract void registerBuilder(String name, String strategyName,
			boolean onSource, boolean meta);

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ALanguage) {
			ALanguage ol = (ALanguage) obj;
			return getName().equals(ol.getName());
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return getName().hashCode() * 31;
	}

	@Override
	public String toString() {
		String s = "";
		s += "Name: " + name + "\n";
		s += "Extensions: " + getFileExtensions() + "\n";
		s += "Parsetable: " + getParseTableProvider() + "\n";
		s += "Code files: \n";
		File[] codefiles = getCompilerFiles();
		for (File file : codefiles) {
			s += "\t " + file.getAbsolutePath() + "\n";
		}
		s += "Observer: " + getAnalysisFunction() + "\n";
		return s;
	}

}
