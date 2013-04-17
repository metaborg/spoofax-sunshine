/**
 * 
 */
package org.spoofax.sunshine.gitdrive;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

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

	SunshineGitArguments params = new SunshineGitArguments();
	parseArguments(args, params);
	if (params.sunshineArgs.help) {
	    usage();
	    System.exit(1);
	}
	params.sunshineArgs.validate();
	logger.info("Execution arguments are \n{}", params);
	org.spoofax.sunshine.drivers.Main.initEnvironment(params.sunshineArgs);

	// the git behavior which wraps the main behavior
	SunshineGitArguments gitArgs = new SunshineGitArguments();
	parseArguments(args, gitArgs);

	SunshineGitDriver driver = new SunshineGitDriver(gitArgs);
	driver.run();

	logger.info("Now exiting");
    }

    private static JCommander jc;

    private static void parseArguments(String[] args, SunshineGitArguments into) {
	logger.trace("Parsing arguments");
	jc = new JCommander();
	jc.setColumnSize(120);
	jc.addObject(into);
	try {
	    jc.parse(args);
	} catch (ParameterException pex) {
	    logger.fatal("Bad command line arguments", pex);
	    pex.printStackTrace();
	    usage();
	}
	logger.trace("Done parsing arguments");
	logger.trace("Done parsing arguments");
    }

    public static void usage() {
	jc.usage();
    }
}
