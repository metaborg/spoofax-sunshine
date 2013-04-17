/**
 * 
 */
package org.spoofax.sunshine.drivers;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spoofax.sunshine.Environment;
import org.spoofax.sunshine.model.language.Language;
import org.spoofax.sunshine.services.LanguageService;

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

	SunshineMainArguments params = new SunshineMainArguments();
	parseArguments(args, params);
	if (params.help) {
	    usage();
	    System.exit(1);
	}
	params.validate();
	logger.info("Execution arguments are \n{}", params);
	initEnvironment(params);
	SunshineMainDriver driver = new SunshineMainDriver();

	driver.run();
	logger.info("Now exiting");
    }

    private static JCommander jc = new JCommander();

    private static void parseArguments(String[] args, SunshineMainArguments into) {
	logger.trace("Parsing arguments");
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
    }

    public static void initEnvironment(SunshineMainArguments args) {
	logger.trace("Initializing the environment");
	Environment env = Environment.INSTANCE();
	env.setMainArguments(args);
	env.setProjectDir(new File(args.project));
	LanguageService.INSTANCE().registerLanguage(
		Language.fromArguments(args.languageArgs));
    }

    public static void usage() {
	jc.usage();
    }

}
