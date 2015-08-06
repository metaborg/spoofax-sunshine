package org.metaborg.sunshine;

import org.metaborg.spoofax.meta.core.SpoofaxMetaModule;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.martiansoftware.nailgun.NGContext;

public class Main {
    private static Injector injector;


    public static void main(String[] args) {
        injector = Guice.createInjector(new SunshineModule()).createChildInjector(new SpoofaxMetaModule());

        run(args, false);
    }

    public static void nailMain(NGContext context) {
        run(context.getArgs(), true);
    }


    private static void run(String[] args, boolean remote) {
        final Runner runner = injector.getInstance(Runner.class);
        final int result = runner.run(args, remote);
        System.exit(result);
    }
}
