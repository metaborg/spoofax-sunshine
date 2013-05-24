package org.spoofax.sunshine.services;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.jsglr.client.imploder.ImploderOriginTermFactory;
import org.spoofax.sunshine.Environment;
import org.spoofax.sunshine.SunshineIOAgent;
import org.spoofax.sunshine.model.language.ALanguage;
import org.spoofax.sunshine.prims.SunshineLibrary;
import org.strategoxt.HybridInterpreter;
import org.strategoxt.IncompatibleJarException;
import org.strategoxt.NoInteropRegistererJarException;

/**
 * Singleton service for the production of language-specific Stratego Interpreters. Precisely one
 * interpreter per languge is cached; subsequent requests for new interpreters are based on the
 * cached ones as prototypes.
 * 
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class RuntimeService {
	private static final String EXTENSION_CTREE = "ctree";
	private static final String EXTENSION_JAR = "jar";

	private static final Logger logger = LogManager.getLogger(RuntimeService.class.getName());

	private static RuntimeService INSTANCE;

	private final Map<ALanguage, HybridInterpreter> prototypes = new HashMap<ALanguage, HybridInterpreter>();

	private RuntimeService() {
	}

	public static RuntimeService INSTANCE() {
		if (INSTANCE == null) {
			INSTANCE = new RuntimeService();
		}
		return INSTANCE;
	}

	/**
	 * @see #getRuntime(ALanguage)
	 */
	public HybridInterpreter getRuntime(File file) {
		return getRuntime(LanguageService.INSTANCE().getLanguageByExten(file));
	}

	/**
	 * Obtain a new {@link HybridInterpreter} for the given {@link ALanguage}. The produced
	 * interpreter is based on an internally cached interpreter instance for the given language. If
	 * such a cache does not exist, then this method first creates an internal cache for the
	 * language and then returns a new interpreter based on that prototype. Note therefore that
	 * multiple calls to this method will return a different interpreter every time.
	 * 
	 * 
	 * @param lang
	 *            The language for which to create a new interpreter.
	 * @return A new interpret for the given language. All of the language's files (
	 *         {@link ALanguage#getCompilerFiles()} are loaded into the interpreter.
	 * 
	 */
	public HybridInterpreter getRuntime(ALanguage lang) {
		HybridInterpreter proto = prototypes.get(lang);
		if (proto == null) {
			proto = createPrototypeRuntime(lang);
		}

		// TODO load overrides and contexts
		final HybridInterpreter interp = new HybridInterpreter(proto, new String[0]);
		interp.getCompiledContext().getExceptionHandler().setEnabled(false);
		interp.init();
		return interp;
	}

	private HybridInterpreter createPrototypeRuntime(ALanguage lang) {
		final HybridInterpreter interp = new HybridInterpreter(new ImploderOriginTermFactory(
				Environment.INSTANCE().termFactory));

		interp.getCompiledContext().getExceptionHandler().setEnabled(false);
		interp.getCompiledContext().registerComponent("stratego_lib");
		interp.getCompiledContext().registerComponent("stratego_sglr");

		interp.addOperatorRegistry(new SunshineLibrary());
		assert interp.getContext().getOperatorRegistry(SunshineLibrary.REGISTRY_NAME) instanceof SunshineLibrary;

		final SunshineIOAgent agent = new SunshineIOAgent();
		agent.setLanguage(lang);
		interp.setIOAgent(agent);
		loadCompilerFiles(interp, lang);

		prototypes.put(lang, interp);

		return interp;
	}

	private static void loadCompilerFiles(HybridInterpreter interp, ALanguage lang) {
		LinkedList<File> jars = new LinkedList<File>();
		LinkedList<File> ctrees = new LinkedList<File>();
		for (File file : lang.getCompilerFiles()) {
			if (FilenameUtils.getExtension(file.getAbsolutePath())
					.equalsIgnoreCase(EXTENSION_CTREE)) {
				ctrees.add(file);
			} else if (FilenameUtils.getExtension(file.getAbsolutePath()).equalsIgnoreCase(
					EXTENSION_JAR)) {
				jars.add(file);
			} else {
				throw new RuntimeException("Unsupported file extension for compiler file: " + file);
			}
		}
		// for some reason the order is important. We must always load the ctrees first (if any).
		if (ctrees.size() > 0)
			loadCompilerCTree(interp, (File[]) ctrees.toArray(new File[ctrees.size()]));
		if (jars.size() > 0)
			loadCompilerJar(interp, (File[]) jars.toArray(new File[jars.size()]));
	}

	private static void loadCompilerJar(HybridInterpreter interp, File[] jars) {
		final URL[] classpath = new URL[jars.length];

		try {
			for (int idx = 0; idx < classpath.length; idx++) {
				File jar = jars[idx];
				jar = jar.isAbsolute() ? jar : jar.getAbsoluteFile();
				classpath[idx] = jar.toURI().toURL();
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

	private static void loadCompilerCTree(HybridInterpreter interp, File[] ctrees) {
		try {
			for (File file : ctrees) {
				logger.trace("Loading ctree {}", file.getPath());
				interp.load(new BufferedInputStream(new FileInputStream(file.getAbsolutePath())));
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to load ctree", e);
		} catch (InterpreterException e) {
			throw new RuntimeException("Failed to load ctree", e);
		}
	}

}
