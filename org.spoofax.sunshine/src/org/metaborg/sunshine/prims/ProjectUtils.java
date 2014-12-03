/**
 * 
 */
package org.metaborg.sunshine.prims;

import java.io.IOException;

import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metaborg.spoofax.core.SpoofaxException;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ILanguageService;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.stratego.StrategoRuntimeService;
import org.metaborg.sunshine.environment.LaunchConfiguration;
import org.metaborg.sunshine.environment.ServiceRegistry;
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
		try {
			ServiceRegistry serviceRegistry = ServiceRegistry.INSTANCE();
			LaunchConfiguration launch = serviceRegistry
					.getService(LaunchConfiguration.class);
			logger.trace("Unloading index store for project {}",
					launch.projectDir.getName().getPath());

			final ILanguage anyLang = serviceRegistry.getService(
					ILanguageService.class).getAny();
			if (anyLang == null)
				return;
			HybridInterpreter runtime = serviceRegistry.getService(
					StrategoRuntimeService.class).getRuntime(anyLang);
			IOperatorRegistry idxLib = runtime.getContext()
					.getOperatorRegistry("INDEX");
			AbstractPrimitive unloadIdxPrim = idxLib.get("LANG_index_unload");

			boolean unloadSuccess = unloadIdxPrim.call(
					runtime.getContext(),
					new Strategy[0],
					new IStrategoTerm[] { runtime.getFactory().makeString(
							launch.projectDir.getName().getPath()) });
			if (!unloadSuccess) {
				throw new SpoofaxException("Could not unload index");
			}
		} catch (Exception ex) {
			logger.warn("Index unload failed", ex);
		}

	}

	public static void unloadTasks() {
		ServiceRegistry serviceRegistry = ServiceRegistry.INSTANCE();
		LaunchConfiguration launch = serviceRegistry
				.getService(LaunchConfiguration.class);
		try {
			logger.trace("Unloading task engine for project {}",
					launch.projectDir.getName().getPath());
			final ILanguage anyLang = serviceRegistry.getService(
					ILanguageService.class).getAny();
			if (anyLang == null)
				return;
			final FileObject projectDir = launch.projectDir;
			serviceRegistry.getService(IStrategoRuntimeService.class)
					.callStratego(
							anyLang,
							"task-unload",
							launch.termFactory.makeString(projectDir.getName()
									.getPath()), projectDir);
		} catch (Exception ex) {
			logger.warn("Task engine unload failed", ex);
		}
	}

	public static void cleanProject() {
		ServiceRegistry serviceRegistry = ServiceRegistry.INSTANCE();
		LaunchConfiguration launch = serviceRegistry
				.getService(LaunchConfiguration.class);
		try {
			launch.cacheDir.delete(new AllFileSelector());
		} catch (IOException ioex) {
			logger.warn("Could not delete cache directory "
					+ launch.cacheDir.getName().getPath(), ioex);
		}
		ProjectUtils.unloadIndex();
		ProjectUtils.unloadTasks();
	}
}
