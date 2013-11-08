/**
 * 
 */
package org.metaborg.sunshine.drivers;

import java.util.LinkedList;
import java.util.List;

import com.beust.jcommander.Parameter;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class SunshineLanguageArguments {

	@Parameter(names = "--lang", description = "[NAME] The name of the language to be loaded")
	public String lang;

	@Parameter(names = "--jar", description = "[PATH] A relative path to a Jar")
	public List<String> jars = new LinkedList<String>();

	@Parameter(names = "--ctree", description = "[PATH] A relative path to a Jar")
	public List<String> ctrees = new LinkedList<String>();

	@Parameter(names = "--table", description = "[PATH] A relative path to a parse table")
	public String tbl;

	@Parameter(names = "--ext", description = "[EXT] A file extension that is supported")
	public List<String> extens = new LinkedList<String>();

	@Parameter(names = "--ssymb", description = "[SORT] A sort to use as the start symbol")
	public String ssymb;

	@Parameter(names = "--observer", description = "[OBS] The name of the strategy to use as an observer")
	public String observer = "editor-analyze";

	public void validate() {
		if (jars.size() == 0 && ctrees.size() == 0) {
			throw new IllegalArgumentException(
					"Neither any ctrees nor any jars specified. Compiler must consist of some files. Use either --ctree or --jar to specify some");
		}
	}

	@Override
	public String toString() {
		String s = "";
		s += "Language name:   " + lang + "\n";
		s += "Language jars:   " + jars + "\n";
		s += "Language ctre:   " + ctrees + "\n";
		s += "Language exts:   " + extens + "\n";
		s += "Language tabl:   " + tbl + "\n";
		s += "Language ssym:   " + ssymb + "\n";
		s += "Language obsv:   " + observer + "\n";
		return s;
	}
}
