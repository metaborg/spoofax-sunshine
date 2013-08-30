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

	@Parameter(names = "--filter", description = "[REGEX] Files whose absolute paths do not match the given regular expression are skipped")
	public String filefilter;

	@Parameter(names = "--build-on", description = "[PATH] Path (relative to project) to invoke the builder on")
	public String filetobuildon;

	@Parameter(names = "--build-on-all", description = "[PATH] A folder (relative to the project) to invoke the builder on all of the files inside.")
	public String filestobuildon;

	@Parameter(names = "--build-on-source", description = "Apply builder to the non-analyzed AST, regardless of whether analysis was performed or not")
	public boolean buildonsource;

	@Parameter(names = "--parse-only", description = "Only parse and report errors, no analysis or compilation")
	public boolean parseonly;

	@Parameter(names = "--no-analysis", description = "Perform no static analysis, linking the builder directly to the parser")
	public boolean noanalysis;

	@Parameter(names = "--legacy-observer", description = "Use an observer with a legacy signature")
	public boolean legacyobserver;

	@Parameter(names = "--non-incremental", description = "Disable incremental processing where applicable")
	public boolean nonincremental;

	@Parameter(names = "--build-with-errors", description = "Call builder even if there are errors after analysis")
	public boolean buildwitherrors;

	@Parameter(names = "--no-warn", description = "Disable reporting of warnings and notes. Only errors are reported.")
	public boolean suppresswarnings;

	@Parameter(names = "--help", help = true)
	public boolean help;

	@ParametersDelegate
	SunshineLanguageArguments languageArgs = new SunshineLanguageArguments();

	public void validate() {
		if (builder == null && (filetobuildon != null || filestobuildon != null)) {
			throw new IllegalArgumentException("No builder to invoke has been specified");
		}
		if (builder != null && (filetobuildon == null && filestobuildon == null)) {
			throw new IllegalArgumentException("No file to apply builder to was given");
		}
		if (parseonly && noanalysis) {
			throw new IllegalArgumentException("Cannot skip analysis in parse-only mode");
		}
		if (parseonly && builder != null) {
			throw new IllegalArgumentException("Cannot apply a builder in parse-only mode");
		}
		if (noanalysis && legacyobserver) {
			throw new IllegalArgumentException(
					"Invalid use of --legacacyobserver in combination with --no-analysis");
		}
		if (buildonsource && builder == null) {
			throw new IllegalArgumentException(
					"Option --build-on-source requires a builder to be specified with --builder");
		}
		if (filetobuildon != null && filestobuildon != null) {
			throw new IllegalArgumentException(
					"Options --build-on and --build-on-all cannot be combined");
		}
		languageArgs.validate();
	}

	@Override
	public String toString() {
		String s = "";
		s += languageArgs.toString();
		s += "Target project: " + project + "\n";
		s += "Builder name: " + builder + "\n";
		s += "Build on file: " + filetobuildon + "\n";
		s += "Rec stats in: " + statstarget + "\n";
		s += "Parse only: " + parseonly + "\n";
		s += "Incremental: " + !nonincremental + "\n";
		s += "Legacy obsrvr:" + legacyobserver + "\n";
		s += "Build on src:" + buildonsource + "\n";
		return s;
	}
}
