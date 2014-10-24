package org.metaborg.sunshine;

import org.metaborg.sunshine.drivers.SunshineMainDriver;
import org.metaborg.sunshine.environment.LaunchConfiguration;
import org.metaborg.sunshine.environment.SunshineMainArguments;
import org.metaborg.sunshine.services.RuntimeService;
import org.metaborg.sunshine.services.StrategoCallService;
import org.metaborg.sunshine.services.analyzer.AnalysisService;
import org.metaborg.sunshine.services.parser.ParserService;
import org.metaborg.sunshine.statistics.Statistics;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class SunshineModule extends AbstractModule {
	private final SunshineMainArguments args;

	public SunshineModule(SunshineMainArguments args) {
		this.args = args;
	}

	@Override
	protected void configure() {
		bind(SunshineMainArguments.class).toInstance(args);
		bind(LaunchConfiguration.class).asEagerSingleton();
		bind(SunshineMainDriver.class).asEagerSingleton();
		bind(RuntimeService.class).asEagerSingleton();
		bind(StrategoCallService.class).asEagerSingleton();
		bind(ParserService.class).asEagerSingleton();
		bind(AnalysisService.class).asEagerSingleton();
		bind(Statistics.class).asEagerSingleton();

		bind(String.class).annotatedWith(
				Names.named("LanguageDiscoveryAnalysisOverride")).toInstance(
				"analysis-cmd");
		bind(ClassLoader.class).annotatedWith(
				Names.named("ResourceClassLoader")).toInstance(
				this.getClass().getClassLoader());
	}
}
