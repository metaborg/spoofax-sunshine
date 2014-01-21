/**
 * 
 */
package org.metaborg.sunshine.prims;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metaborg.sunshine.CompilerException;
import org.metaborg.sunshine.environment.LaunchConfiguration;
import org.metaborg.sunshine.environment.ServiceRegistry;
import org.metaborg.sunshine.services.RuntimeService;
import org.metaborg.sunshine.services.StrategoCallService;
import org.metaborg.sunshine.services.language.LanguageService;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.library.IOperatorRegistry;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.HybridInterpreter;

/**
 * @author vladvergu
 * 
 */
public class ProjectUtils {

	private static final Logger logger = LogManager
			.getLogger(ProjectUtils.class.getName());

	public static void unloadIndex() {
		ServiceRegistry serviceRegistry = ServiceRegistry.INSTANCE();
		LaunchConfiguration launch = serviceRegistry
				.getService(LaunchConfiguration.class);
		logger.trace("Unloading index store for project {}",
				launch.projectDir.getAbsolutePath());

		HybridInterpreter runtime = serviceRegistry.getService(
				RuntimeService.class).getRuntime(
				serviceRegistry.getService(LanguageService.class)
						.getAnyLanguage());
		IOperatorRegistry idxLib = runtime.getContext().getOperatorRegistry(
				"INDEX");
		AbstractPrimitive unloadIdxPrim = idxLib.get("LANG_index_unload");
		try {
			boolean unloadSuccess = unloadIdxPrim.call(runtime.getContext(),
					new Strategy[0], new IStrategoTerm[] { runtime.getFactory()
							.makeString(launch.projectDir.getAbsolutePath()) });
			if (!unloadSuccess) {
				throw new CompilerException("Could not unload index");
			}
		} catch (InterpreterException intex) {
			throw new CompilerException("Could not unload index", intex);
		}

	}

	public static void unloadTasks() {
		ServiceRegistry serviceRegistry = ServiceRegistry.INSTANCE();
		LaunchConfiguration launch = serviceRegistry
				.getService(LaunchConfiguration.class);
		logger.trace("Unloading task engine for project {}",
				launch.projectDir.getAbsolutePath());
		serviceRegistry.getService(StrategoCallService.class).callStratego(
				serviceRegistry.getService(LanguageService.class)
						.getAnyLanguage(),
				"task-unload",
				launch.termFactory.makeString(launch.projectDir
						.getAbsolutePath()));
	}

	public static void cleanProject() {
		ServiceRegistry serviceRegistry = ServiceRegistry.INSTANCE();
		LaunchConfiguration launch = serviceRegistry
				.getService(LaunchConfiguration.class);
		try {
			FileUtils.deleteDirectory(launch.getCacheDir());
		} catch (IOException ioex) {
			logger.fatal(
					"Could not delete cache directory {} because of exception {}",
					launch.getCacheDir(), ioex);
			throw new CompilerException("Could not delete cache directory",
					ioex);
		}
		ProjectUtils.unloadIndex();
		ProjectUtils.unloadTasks();
	}

	public static File saveProjectState() {
		logger.trace("Saving cache folder to a temporary directory");
		ServiceRegistry serviceRegistry = ServiceRegistry.INSTANCE();
		LaunchConfiguration launch = serviceRegistry
				.getService(LaunchConfiguration.class);
		File cacheDir = launch.getCacheDir();
		File tempDir = new File(FileUtils.getTempDirectory(), "_sunshine_"
				+ System.currentTimeMillis());
		if (cacheDir.exists() && cacheDir.isDirectory()) {
			try {
				FileUtils.moveDirectory(cacheDir, tempDir);
			} catch (IOException ioex) {
				logger.fatal(
						"Could not move cache dir out of the way because of exception",
						ioex);
				throw new RuntimeException("Failed to move cache directory",
						ioex);
			}
			logger.trace("Moved cache dir {} to temporary {}", cacheDir,
					tempDir);
			return tempDir;
		} else {
			logger.warn("Failed to save cacheDir {} because it does not exist",
					cacheDir);
			return null;
		}
	}

	public static void restoreProjectState(File tmp) {
		ServiceRegistry serviceRegistry = ServiceRegistry.INSTANCE();
		LaunchConfiguration launch = serviceRegistry
				.getService(LaunchConfiguration.class);
		File cacheDir = launch.getCacheDir();
		logger.trace("Restoring saved cache {} to original location {}", tmp,
				cacheDir);
		try {
			if (cacheDir.exists())
				FileUtils.deleteDirectory(cacheDir);
			FileUtils.moveDirectory(tmp, cacheDir);
		} catch (IOException ioex) {
			logger.fatal(
					"Could not move cache dir back in the project because of exception",
					ioex);
			throw new RuntimeException("Failed to restore saved cache dir",
					ioex);
		}
	}
}
