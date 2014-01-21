/**
 * 
 */
package org.metaborg.sunshine.drivers;

import java.io.File;
import java.nio.file.FileSystems;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metaborg.sunshine.Environment;
import org.metaborg.sunshine.services.language.LanguageDiscoveryService;
import org.metaborg.sunshine.services.language.LanguageService;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class Main {

	private static final Logger logger = LogManager.getLogger(Main.class.getName());

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		logger.info("Starting");

		SunshineMainArguments params = new SunshineMainArguments();
		parseArguments(args, params);
		if (params.help) {
			usage(true);
		}
		params.validate();
		logger.info("Execution arguments are \n{}", params);
		initEnvironment(params);
		SunshineMainDriver driver = new SunshineMainDriver();

		int exit = driver.run();
		if (exit == 0) {
			logger.info("Exiting normally");
			System.exit(0);
		} else {
			logger.info("Exiting with non-zero status {}", exit);
			System.exit(1);
		}
	}

	private static JCommander jc = new JCommander();

	public static void parseArguments(String[] args, SunshineMainArguments into) {
		logger.trace("Parsing arguments");
		jc.setColumnSize(120);
		jc.addObject(into);
		try {
			jc.parse(args);
		} catch (ParameterException pex) {
			System.err.println(pex.getMessage());
			usage(true);
		}
		logger.trace("Done parsing arguments");
	}

	public static void initEnvironment(SunshineMainArguments args) {
		logger.trace("Initializing the environment");
		Environment env = Environment.INSTANCE();
		env.setMainArguments(args);
		env.setProjectDir(new File(args.project));
		if (args.autolang != null) {
			LanguageDiscoveryService.INSTANCE().discover(
					FileSystems.getDefault().getPath(args.autolang));
		} else {
			LanguageService.INSTANCE().registerLanguage(
					LanguageDiscoveryService.INSTANCE().languageFromArguments(args.languageArgs));
		}
	}

	public static void usage(boolean exit) {
		jc.usage();
		if (exit)
			System.exit(1);
	}

}
