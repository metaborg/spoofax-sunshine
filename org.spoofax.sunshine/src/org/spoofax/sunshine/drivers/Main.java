/**
 * 
 */
package org.spoofax.sunshine.drivers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spoofax.sunshine.Environment;
import org.spoofax.sunshine.LaunchConfiguration;

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
	logger.error("Starting");
	LaunchConfiguration launch = CLIArgumentsParser.parseArgs(args);
	Environment.INSTANCE().setLaunchConfiguration(launch);
	SunshineMainDriver driver = new SunshineMainDriver();
	driver.run();
	logger.error("Now exiting");
    }

}
