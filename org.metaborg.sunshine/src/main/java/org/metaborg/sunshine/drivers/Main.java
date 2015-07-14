package org.metaborg.sunshine.drivers;

import java.io.IOException;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguage;
import org.metaborg.core.language.ILanguageDiscoveryService;
import org.metaborg.core.language.ILanguageService;
import org.metaborg.core.language.IdentificationFacet;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.language.LanguageVersion;
import org.metaborg.core.language.ResourceExtensionFacet;
import org.metaborg.core.language.ResourceExtensionsIdentifier;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.core.analysis.StrategoAnalysisMode;
import org.metaborg.spoofax.core.stratego.StrategoFacet;
import org.metaborg.spoofax.core.syntax.SyntaxFacet;
import org.metaborg.sunshine.SunshineModule;
import org.metaborg.sunshine.environment.ServiceRegistry;
import org.metaborg.sunshine.environment.SunshineLanguageArguments;
import org.metaborg.sunshine.environment.SunshineMainArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
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
                final SunshineLanguageArguments langArgs = args.getLanguageArgs();
                final FileObject tempDirectory = resourceService.resolve("tmp:///");
                tempDirectory.createFolder();
                createLanguage(langArgs.lang, new LanguageVersion(1, 0, 0, ""), tempDirectory,
                    ImmutableSet.copyOf(langArgs.extens), resourceService.resolve(langArgs.tbl), langArgs.ssymb,
                    ImmutableSet.copyOf(resourceService.resolveAll(langArgs.ctrees)),
                    ImmutableSet.copyOf(resourceService.resolveAll(langArgs.jars)), langArgs.observer);
            }
        } catch(Exception e) {
            logger.error("Failed to discover language", e);
        }
    }

    private static ILanguage createLanguage(String name, LanguageVersion version, FileObject location,
        ImmutableSet<String> extensions, FileObject parseTable, String startSymbol,
        ImmutableSet<FileObject> ctreeFiles, ImmutableSet<FileObject> jarFiles, String analysisStrategy) {
        logger.debug("Creating language {} from custom parameters", name);

        final ILanguageService languageService = env.getService(ILanguageService.class);
        final ILanguage language = languageService.create(new LanguageIdentifier(name, name, version), location, name);

        final IdentificationFacet identificationFacet =
            new IdentificationFacet(new ResourceExtensionsIdentifier(extensions));
        language.addFacet(identificationFacet);

        final ResourceExtensionFacet resourceExtensionsFacet = new ResourceExtensionFacet(extensions);
        language.addFacet(resourceExtensionsFacet);

        final SyntaxFacet syntaxFacet = new SyntaxFacet(parseTable, Sets.newHashSet(startSymbol));
        language.addFacet(syntaxFacet);

        final StrategoFacet strategoFacet =
            new StrategoFacet(ctreeFiles, jarFiles, analysisStrategy, StrategoAnalysisMode.MultiAST, null, null, null,
                null);
        language.addFacet(strategoFacet);

        languageService.add(language);

        return language;
    }

    public static void usage(boolean exit) {
        jc.usage();
        if(exit)
            System.exit(1);
    }
}
