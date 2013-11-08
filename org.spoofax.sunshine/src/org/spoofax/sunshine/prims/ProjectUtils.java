/**
 * 
 */
package org.spoofax.sunshine.prims;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.library.IOperatorRegistry;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.sunshine.CompilerException;
import org.spoofax.sunshine.Environment;
import org.spoofax.sunshine.services.RuntimeService;
import org.spoofax.sunshine.services.StrategoCallService;
import org.spoofax.sunshine.services.language.LanguageService;
import org.strategoxt.HybridInterpreter;

/**
 * @author vladvergu
 * 
 */
public class ProjectUtils {

	private static final Logger logger = LogManager.getLogger(ProjectUtils.class.getName());

	public static void unloadIndex() {
		logger.trace("Unloading index store for project {}",
				Environment.INSTANCE().projectDir.getAbsolutePath());

		HybridInterpreter runtime = RuntimeService.INSTANCE().getRuntime(
				LanguageService.INSTANCE().getAnyLanguage());
		IOperatorRegistry idxLib = runtime.getContext().getOperatorRegistry("INDEX");
		AbstractPrimitive unloadIdxPrim = idxLib.get("LANG_index_unload");
		try {
			boolean unloadSuccess = unloadIdxPrim.call(
					runtime.getContext(),
					new Strategy[0],
					new IStrategoTerm[] { runtime.getFactory().makeString(
							Environment.INSTANCE().projectDir.getAbsolutePath()) });
			if (!unloadSuccess) {
				throw new CompilerException("Could not unload index");
			}
		} catch (InterpreterException intex) {
			throw new CompilerException("Could not unload index", intex);
		}

	}

	public static void unloadTasks() {
		logger.trace("Unloading task engine for project {}",
				Environment.INSTANCE().projectDir.getAbsolutePath());
		StrategoCallService.INSTANCE().callStratego(
				LanguageService.INSTANCE().getAnyLanguage(),
				"task-unload",
				Environment.INSTANCE().termFactory.makeString(Environment.INSTANCE().projectDir
						.getAbsolutePath()));
	}

	public static void cleanProject() {
		try {
			FileUtils.deleteDirectory(Environment.INSTANCE().getCacheDir());
		} catch (IOException ioex) {
			logger.fatal("Could not delete cache directory {} because of exception {}", Environment
					.INSTANCE().getCacheDir(), ioex);
			throw new CompilerException("Could not delete cache directory", ioex);
		}
		ProjectUtils.unloadIndex();
		ProjectUtils.unloadTasks();
	}

	public static File saveProjectState() {
		logger.trace("Saving cache folder to a temporary directory");
		File cacheDir = Environment.INSTANCE().getCacheDir();
		File tempDir = new File(FileUtils.getTempDirectory(), "_sunshine_"
				+ System.currentTimeMillis());
		if (cacheDir.exists() && cacheDir.isDirectory()) {
			try {
				FileUtils.moveDirectory(cacheDir, tempDir);
			} catch (IOException ioex) {
				logger.fatal("Could not move cache dir out of the way because of exception", ioex);
				throw new RuntimeException("Failed to move cache directory", ioex);
			}
			logger.trace("Moved cache dir {} to temporary {}", cacheDir, tempDir);
			return tempDir;
		} else {
			logger.warn("Failed to save cacheDir {} because it does not exist", cacheDir);
			return null;
		}
	}

	public static void restoreProjectState(File tmp) {
		File cacheDir = Environment.INSTANCE().getCacheDir();
		logger.trace("Restoring saved cache {} to original location {}", tmp, cacheDir);
		try {
			if (cacheDir.exists())
				FileUtils.deleteDirectory(cacheDir);
			FileUtils.moveDirectory(tmp, cacheDir);
		} catch (IOException ioex) {
			logger.fatal("Could not move cache dir back in the project because of exception", ioex);
			throw new RuntimeException("Failed to restore saved cache dir", ioex);
		}
	}
}
