package org.metaborg.sunshine.drivers;

import java.io.IOException;

import org.metaborg.core.language.ILanguageDiscoveryService;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.sunshine.SunshineModule;
import org.metaborg.sunshine.environment.ServiceRegistry;
import org.metaborg.sunshine.environment.SunshineMainArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.google.inject.Guice;
import com.google.inject.Injector;


public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class.getName());
    private static final ServiceRegistry env = ServiceRegistry.INSTANCE();

    public static void main(String[] args) {
        logger.info("Starting");
        jc = new JCommander();
        SunshineMainArguments params = new SunshineMainArguments();
        boolean argsFine = parseArguments(args, params);
        if(params.help || !argsFine) {
            usage(true);
        }
        params.validate();
        logger.info("Execution arguments are \n{}", params);
        initEnvironment(params);
        discoverLanguages(params);
        final SunshineMainDriver driver = env.getService(SunshineMainDriver.class);

        try {
            int exit = driver.run();
            if(exit == 0) {
                logger.info("Exiting normally");
                System.exit(0);
            } else {
                logger.info("Exiting with non-zero status {}", exit);
                System.exit(1);
            }
        } catch(IOException e) {
            logger.error("Failed to run driver", e);
        }
    }

    public static JCommander jc;

    public static boolean parseArguments(String[] args, SunshineMainArguments into) {
        logger.trace("Parsing arguments");
        jc.setColumnSize(120);
        jc.addObject(into);
        try {
            jc.parse(args);
            logger.trace("Done parsing arguments");
            return true;
        } catch(ParameterException pex) {
            System.err.println(pex.getMessage());
            pex.printStackTrace();
            return false;
        }
    }

    public static void initEnvironment(SunshineMainArguments args) {
        logger.trace("Initializing the environment");
        final Injector injector = Guice.createInjector(new SunshineModule(args));
        env.setInjector(injector);
    }

    public static void discoverLanguages(SunshineMainArguments args) {
        final ILanguageDiscoveryService langDiscovery = env.getService(ILanguageDiscoveryService.class);
        final IResourceService resourceService = env.getService(IResourceService.class);

        try {
            if(args.autolang != null) {
                langDiscovery.discover(resourceService.resolve(args.autolang));
            } else {
                logger.error("Custom language creation not supported any more");
            }
        } catch(Exception e) {
            logger.error("Failed to discover language", e);
        }
    }



    public static void usage(boolean exit) {
        jc.usage();
        if(exit)
            System.exit(1);
    }
}
