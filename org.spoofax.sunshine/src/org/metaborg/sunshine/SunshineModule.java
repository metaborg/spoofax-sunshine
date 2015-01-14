package org.metaborg.sunshine;

import org.apache.commons.vfs2.FileSystemManager;
import org.metaborg.runtime.task.primitives.TaskLibrary;
import org.metaborg.spoofax.core.SpoofaxModule;
import org.metaborg.spoofax.core.resource.IResourceService;
import org.metaborg.spoofax.core.resource.ResourceService;
import org.metaborg.spoofax.core.stratego.StrategoRuntimeService;
import org.metaborg.sunshine.drivers.SunshineMainDriver;
import org.metaborg.sunshine.environment.LaunchConfiguration;
import org.metaborg.sunshine.environment.SunshineMainArguments;
import org.metaborg.sunshine.prims.SunshineLibrary;
import org.metaborg.sunshine.services.filesource.SunshineFileSystemManagerProvider;
import org.metaborg.sunshine.statistics.Statistics;
import org.spoofax.interpreter.library.IOperatorRegistry;
import org.spoofax.interpreter.library.index.legacy.LegacyIndexLibrary;

import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

public class SunshineModule extends SpoofaxModule {
	private final SunshineMainArguments args;

	public SunshineModule(SunshineMainArguments args) {
		this.args = args;
	}

	@Override
	protected void bindResource() {
		bind(IResourceService.class).to(ResourceService.class).in(
				Singleton.class);
		bind(FileSystemManager.class).toProvider(
				SunshineFileSystemManagerProvider.class).in(Singleton.class);
	}

	@Override
	protected void bindOther() {
		bind(SunshineMainArguments.class).toInstance(args);
		bind(LaunchConfiguration.class).asEagerSingleton();
		bind(SunshineMainDriver.class).asEagerSingleton();
		bind(StrategoRuntimeService.class).asEagerSingleton();
		bind(Statistics.class).asEagerSingleton();

		final Multibinder<IOperatorRegistry> strategoLibraryBinder = Multibinder
				.newSetBinder(binder(), IOperatorRegistry.class);
		strategoLibraryBinder.addBinding().toInstance(new TaskLibrary());
		strategoLibraryBinder.addBinding().toInstance(new LegacyIndexLibrary());
		strategoLibraryBinder.addBinding().toInstance(new SunshineLibrary());

		bind(String.class).annotatedWith(
				Names.named("LanguageDiscoveryAnalysisOverride")).toInstance(
				"analysis-cmd");
		bind(ClassLoader.class).annotatedWith(
				Names.named("ResourceClassLoader")).toInstance(
				this.getClass().getClassLoader());
	}
}
