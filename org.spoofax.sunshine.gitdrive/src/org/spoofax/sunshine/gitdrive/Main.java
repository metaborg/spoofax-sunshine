/**
 * 
 */
package org.spoofax.sunshine.gitdrive;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spoofax.sunshine.Environment;
import org.spoofax.sunshine.LaunchConfiguration;
import org.spoofax.sunshine.drivers.CLIArgumentsParser;

import com.beust.jcommander.JCommander;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class Main {

    private static final Logger logger = LogManager.getLogger(Main.class
	    .getName());

    /**
     * @param args
     */
    public static void main(String[] args) {
	logger.info("Starting");

	// the main behavior
	LaunchConfiguration launch = CLIArgumentsParser.parseArgs(args);
	Environment.INSTANCE().setLaunchConfiguration(launch);

	// the git behavior which wraps the main behavior
	GitRunArguments gitArgs = new GitRunArguments();
	parseGitArgs(args, gitArgs);
	SunshineGitDriver driver = new SunshineGitDriver(gitArgs);
	driver.run();

	logger.info("Now exiting");
    }

    private static void parseGitArgs(String[] args, GitRunArguments into) {
	logger.trace("Parsing arguments");
	JCommander jc = new JCommander();
	jc.setAcceptUnknownOptions(true);
	jc.addObject(into);
	jc.parse(args);
	logger.trace("Done parsing arguments");

    }

}
