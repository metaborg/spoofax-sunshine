package org.metaborg.sunshine;

import org.metaborg.spoofax.core.SpoofaxModule;
import org.metaborg.spoofax.core.stratego.StrategoRuntimeService;
import org.metaborg.sunshine.drivers.SunshineMainDriver;
import org.metaborg.sunshine.environment.LaunchConfiguration;
import org.metaborg.sunshine.environment.SunshineMainArguments;
import org.metaborg.sunshine.prims.SunshineLibrary;
import org.metaborg.sunshine.statistics.Statistics;
import org.spoofax.interpreter.library.IOperatorRegistry;

import com.google.inject.multibindings.Multibinder;

public class SunshineModule extends SpoofaxModule {
    private final SunshineMainArguments args;

    
    public SunshineModule(SunshineMainArguments args) {
        this.args = args;
    }


    @Override protected void configure() {
        super.configure();

        bind(SunshineMainArguments.class).toInstance(args);
        bind(LaunchConfiguration.class).asEagerSingleton();
        bind(SunshineMainDriver.class).asEagerSingleton();
        bind(StrategoRuntimeService.class).asEagerSingleton();
        bind(Statistics.class).asEagerSingleton();

        final Multibinder<IOperatorRegistry> strategoLibraryBinder =
            Multibinder.newSetBinder(binder(), IOperatorRegistry.class);
        strategoLibraryBinder.addBinding().toInstance(new SunshineLibrary());
    }
}
