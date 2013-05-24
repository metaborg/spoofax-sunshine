/**
 * 
 */
package org.spoofax.sunshine.drivers;

import java.util.LinkedList;
import java.util.List;

import com.beust.jcommander.Parameter;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class SunshineLanguageArguments {

	@Parameter(names = "--lang", description = "[NAME] The name of the language to be loaded", required = true)
	public String lang;

	@Parameter(names = "--jar", description = "[PATH] A relative path to a Jar", required = true)
	public List<String> jars = new LinkedList<String>();

	@Parameter(names = "--table", description = "[PATH] A relative path to a parse table", required = true)
	public String tbl;

	@Parameter(names = "--ext", description = "[EXT] A file extension that is supported", required = true)
	public List<String> extens = new LinkedList<String>();

	@Parameter(names = "--ssymb", description = "[SORT] A sort to use as the start symbol", required = true)
	public String ssymb;

	@Parameter(names = "--observer", description = "[OBS] The name of the strategy to use as an observer")
	public String observer = "editor-analyze";

	@Override
	public String toString() {
		String s = "";
		s += "Language name: " + lang + "\n";
		s += "Language jars: " + jars + "\n";
		s += "Language exts: " + extens + "\n";
		s += "Language tabl: " + tbl + "\n";
		s += "Language ssym: " + ssymb + "\n";
		s += "Language obsv: " + observer + "\n";
		return s;
	}
}
