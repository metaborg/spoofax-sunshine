/**
 * 
 */
package org.spoofax.sunshine.drivers;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class SunshineMainArguments {
	@Parameter(names = "--project", description = "[PATH] The basedirectory of the project to run on", required = true)
	public String project;

	@Parameter(names = "--builder", description = "[NAME] The name of the builder to invoke on the file")
	public String builder;

	@Parameter(names = "--stats", description = "[PATH] Path to a file to store statistics in. Automatically enables statistics")
	public String statstarget;

	@Parameter(names = "--build-on", description = "[PATH] Path (relative to project) to invoke the builder on")
	public String tobuildfile;

	@Parameter(names = "--parse-only", description = "Only parse and report errors, no analysis or compilation")
	public boolean parseonly;

	@Parameter(names = "--help", help = true)
	public boolean help;

	@Parameter(names = "--non-incremental", description = "Disable incremental processing where applicable")
	public boolean nonincremental = false;

	@ParametersDelegate
	SunshineLanguageArguments languageArgs = new SunshineLanguageArguments();

	public void validate() {
		if (builder == null && tobuildfile != null) {
			throw new IllegalArgumentException("No builder to invoke has been specified");
		}
		if (builder != null && tobuildfile == null) {
			throw new IllegalArgumentException("No file to apply builder to was given");
		}
		if (parseonly && builder != null) {
			throw new IllegalArgumentException("Cannot apply a builder in parse-only mode");
		}
	}

	@Override
	public String toString() {
		String s = "";
		s += languageArgs.toString();
		s += "Target project: " + project + "\n";
		s += "Builder name: " + builder + "\n";
		s += "Build on file: " + tobuildfile + "\n";
		s += "Rec stats in: " + statstarget + "\n";
		s += "Parse only: " + parseonly + "\n";
		s += "Incremental: " + !nonincremental + "\n";
		return s;
	}
}
