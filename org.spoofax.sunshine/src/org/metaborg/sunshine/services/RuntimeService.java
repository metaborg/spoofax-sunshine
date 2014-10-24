package org.metaborg.sunshine.services;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metaborg.runtime.task.primitives.TaskLibrary;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ILanguageIdentifierService;
import org.metaborg.spoofax.core.service.stratego.StrategoFacet;
import org.metaborg.sunshine.SunshineIOAgent;
import org.metaborg.sunshine.environment.LaunchConfiguration;
import org.metaborg.sunshine.environment.ServiceRegistry;
import org.metaborg.sunshine.prims.SunshineLibrary;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.index.legacy.LegacyIndexLibrary;
import org.spoofax.jsglr.client.imploder.ImploderOriginTermFactory;
import org.strategoxt.HybridInterpreter;
import org.strategoxt.IncompatibleJarException;
import org.strategoxt.NoInteropRegistererJarException;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;

/**
 * Singleton service for the production of language-specific Stratego
 * Interpreters. Precisely one
 * interpreter per languge is cached; subsequent requests for new interpreters
 * are based on the
 * cached ones as prototypes.
 * 
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class RuntimeService {
	private static final Logger logger = LogManager
			.getLogger(RuntimeService.class.getName());

	private final ILanguageIdentifierService languageIdentifierService;

	private final Map<ILanguage, HybridInterpreter> prototypes = new HashMap<ILanguage, HybridInterpreter>();

	@Inject
	public RuntimeService(
			ILanguageIdentifierService languageIdentifierService) {
		this.languageIdentifierService = languageIdentifierService;
	}

	/**
	 * @see #getRuntime(ALanguage)
	 */
	public HybridInterpreter getRuntime(FileObject file) {
		return getRuntime(languageIdentifierService.identify(file));
	}

	/**
	 * Obtain a new {@link HybridInterpreter} for the given {@link ALanguage}.
	 * The produced
	 * interpreter is based on an internally cached interpreter instance for the
	 * given language. If
	 * such a cache does not exist, then this method first creates an internal
	 * cache for the
	 * language and then returns a new interpreter based on that prototype. Note
	 * therefore that
	 * multiple calls to this method will return a different interpreter every
	 * time.
	 * 
	 * 
	 * @param lang
	 *            The language for which to create a new interpreter.
	 * @return A new interpret for the given language. All of the language's
	 *         files ( {@link ALanguage#getCompilerFiles()} are loaded into the
	 *         interpreter.
	 * 
	 */
	public HybridInterpreter getRuntime(ILanguage lang) {
		HybridInterpreter proto = prototypes.get(lang);
		if (proto == null) {
			proto = createPrototypeRuntime(lang);
		}

		// TODO load overrides and contexts
		final HybridInterpreter interp = new HybridInterpreter(proto,
				new String[0]);
		interp.getCompiledContext().getExceptionHandler().setEnabled(false);
		interp.init();

		return interp;
	}

	private HybridInterpreter createPrototypeRuntime(ILanguage lang) {
		final HybridInterpreter interp = new HybridInterpreter(
				new ImploderOriginTermFactory(ServiceRegistry.INSTANCE()
						.getService(LaunchConfiguration.class).termFactory));

		interp.getCompiledContext().registerComponent("stratego_lib");
		interp.getCompiledContext().registerComponent("stratego_sglr");
		interp.getCompiledContext().addOperatorRegistry(new TaskLibrary());
		interp.getCompiledContext().addOperatorRegistry(
				new LegacyIndexLibrary());

		interp.addOperatorRegistry(new SunshineLibrary());
		assert interp.getContext().getOperatorRegistry(
				SunshineLibrary.REGISTRY_NAME) instanceof SunshineLibrary;

		final SunshineIOAgent agent = new SunshineIOAgent(lang);
		interp.setIOAgent(agent);
		loadCompilerFiles(interp, lang);

		prototypes.put(lang, interp);

		return interp;
	}

	private static void loadCompilerFiles(HybridInterpreter interp,
			ILanguage lang) {
		final StrategoFacet strategoFacet = lang.facet(StrategoFacet.class);
		final Iterable<FileObject> jars = strategoFacet.jarFiles();
		final Iterable<FileObject> ctrees = strategoFacet.ctreeFiles();

		// for some reason the order is important. We must always load the
		// ctrees first (if any).
		if (Iterables.size(ctrees) > 0)
			loadCompilerCTree(interp, ctrees);
		if (Iterables.size(jars) > 0)
			loadCompilerJar(interp, jars);
	}

	private static void loadCompilerJar(HybridInterpreter interp,
			Iterable<FileObject> jars) {
		try {
			final URL[] classpath = new URL[Iterables.size(jars)];
			int i = 0;
			for (FileObject jar : jars) {
				classpath[i] = jar.getURL();
			}
			logger.trace("Loading jar files {}", (Object) classpath);
			interp.loadJars(classpath);
		} catch (MalformedURLException e) {
			throw new RuntimeException("Failed to load jar", e);
		} catch (SecurityException e) {
			throw new RuntimeException("Failed to load jar", e);
		} catch (NoInteropRegistererJarException e) {
			throw new RuntimeException("Failed to load jar", e);
		} catch (IncompatibleJarException e) {
			throw new RuntimeException("Failed to load jar", e);
		} catch (IOException e) {
			throw new RuntimeException("Failed to load jar", e);
		}
	}

	private static void loadCompilerCTree(HybridInterpreter interp,
			Iterable<FileObject> ctrees) {
		try {
			for (FileObject file : ctrees) {
				logger.trace("Loading ctree {}", file.getName());
				interp.load(new BufferedInputStream(file.getContent()
						.getInputStream()));
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to load ctree", e);
		} catch (InterpreterException e) {
			throw new RuntimeException("Failed to load ctree", e);
		}
	}

}
