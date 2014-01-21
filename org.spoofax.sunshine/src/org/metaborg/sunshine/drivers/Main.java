/**
 * 
 */
package org.metaborg.sunshine.drivers;

import java.io.File;
import java.nio.file.FileSystems;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metaborg.sunshine.environment.LaunchConfiguration;
import org.metaborg.sunshine.environment.ServiceRegistry;
import org.metaborg.sunshine.environment.SunshineMainArguments;
import org.metaborg.sunshine.services.RuntimeService;
import org.metaborg.sunshine.services.StrategoCallService;
import org.metaborg.sunshine.services.analyzer.AnalysisService;
import org.metaborg.sunshine.services.language.LanguageDiscoveryService;
import org.metaborg.sunshine.services.language.LanguageService;
import org.metaborg.sunshine.services.parser.ParserService;
import org.metaborg.sunshine.statistics.Statistics;

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
		jc = new JCommander();
		SunshineMainArguments params = new SunshineMainArguments();
		boolean argsFine = parseArguments(args, params);
		if (params.help || !argsFine) {
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

	public static JCommander jc;

	public static boolean parseArguments(String[] args,
			SunshineMainArguments into) {
		logger.trace("Parsing arguments");
		jc.setColumnSize(120);
		jc.addObject(into);
		try {
			jc.parse(args);
			logger.trace("Done parsing arguments");
			return true;
		} catch (ParameterException pex) {
			System.err.println(pex.getMessage());
			pex.printStackTrace();
			return false;
		}
	}

	public static void initEnvironment(SunshineMainArguments args) {
		logger.trace("Initializing the environment");
		ServiceRegistry env = ServiceRegistry.INSTANCE();
		env.reset();

		initServices(env, args);

		LanguageDiscoveryService langDiscovery = env
				.getService(LanguageDiscoveryService.class);
		LanguageService langService = env.getService(LanguageService.class);
		assert langDiscovery != null;
		if (args.autolang != null) {
			langDiscovery.discover(FileSystems.getDefault().getPath(
					args.autolang));
		} else {
			langService.registerLanguage(langDiscovery
					.languageFromArguments(args.getLanguageArgs()));
		}
	}

	public static void initServices(ServiceRegistry env,
			SunshineMainArguments args) {

		env.registerService(LaunchConfiguration.class, new LaunchConfiguration(
				args, new File(args.project)));
		env.registerService(LanguageDiscoveryService.class,
				new LanguageDiscoveryService());
		env.registerService(LanguageService.class, new LanguageService());
		env.registerService(RuntimeService.class, new RuntimeService());
		env.registerService(StrategoCallService.class,
				new StrategoCallService());
		env.registerService(ParserService.class, new ParserService());
		env.registerService(AnalysisService.class, new AnalysisService());
		env.registerService(Statistics.class, new Statistics());
	}

	public static void usage(boolean exit) {
		jc.usage();
		if (exit)
			System.exit(1);
	}

}
