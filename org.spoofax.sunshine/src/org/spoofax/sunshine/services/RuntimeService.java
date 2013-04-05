package org.spoofax.sunshine.services;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

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
		
		switch (lang.getNature()) {
		case CTREE_NATURE:
			loadCompilerCTree(interp, lang);
			break;
		case JAR_NATURE:
			loadCompilerJar(interp, lang);
			break;
		default:
			throw new RuntimeException("Unsupported language nature " + lang.getNature());
		}

		prototypes.put(lang, interp);

		return interp;
	}

	private static void loadCompilerJar(HybridInterpreter interp, ALanguage lang) {
		final File[] jars = lang.getCompilerFiles();
		final URL[] classpath = new URL[jars.length];

		try {
			for (int idx = 0; idx < classpath.length; idx++) {
				File jar = jars[idx];
				jar = jar.isAbsolute() ? jar : jar.getAbsoluteFile();
				classpath[idx] = jar.toURI().toURL();
			}
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

	private static void loadCompilerCTree(HybridInterpreter interp, ALanguage lang) {
		// TODO Auto-generated method stub
		throw new RuntimeException("CTree-based compilers are not supported yet");
	}

}
