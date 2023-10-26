package org.metaborg.sunshine;

import org.metaborg.core.MetaborgModule;
import org.metaborg.core.editor.IEditorRegistry;
import org.metaborg.core.editor.NullEditorRegistry;
import org.metaborg.core.project.IProjectService;
import org.metaborg.core.project.ISimpleProjectService;
import org.metaborg.core.project.SimpleProjectService;
import org.metaborg.spoofax.core.SpoofaxModule;
import org.metaborg.sunshine.arguments.InputDelegate;
import org.metaborg.sunshine.arguments.LanguagesDelegate;
import org.metaborg.sunshine.arguments.ProjectPathDelegate;
import org.metaborg.sunshine.command.local.LocalAnalyzeCommand;
import org.metaborg.sunshine.command.local.LocalBuildCommand;
import org.metaborg.sunshine.command.local.LocalParseCommand;
import org.metaborg.sunshine.command.local.LocalTransformCommand;
import org.metaborg.sunshine.command.local.ServerCommand;
import org.metaborg.sunshine.command.remote.LoadLanguageCommand;
import org.metaborg.sunshine.command.remote.RemoteAnalyzeCommand;
import org.metaborg.sunshine.command.remote.RemoteBuildCommand;
import org.metaborg.sunshine.command.remote.RemoteParseCommand;
import org.metaborg.sunshine.command.remote.RemoteTransformCommand;
import org.metaborg.sunshine.common.LanguageLoader;

import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Names;

import spoofax.core.cmd.command.ICommand;

public class SunshineModule extends SpoofaxModule {
    @Override protected void configure() {
        super.configure();

        bind(LanguageLoader.class).in(Singleton.class);

        final MapBinder<String, ICommand> localCommands =
            MapBinder.newMapBinder(binder(), String.class, ICommand.class, Names.named("local"));
        localCommands.addBinding("parse").to(LocalParseCommand.class).in(Singleton.class);
        localCommands.addBinding("analyze").to(LocalAnalyzeCommand.class).in(Singleton.class);
        localCommands.addBinding("transform").to(LocalTransformCommand.class).in(Singleton.class);
        localCommands.addBinding("build").to(LocalBuildCommand.class).in(Singleton.class);
        localCommands.addBinding("server").to(ServerCommand.class).in(Singleton.class);

        // Don't use Singleton scope for remote commands, delegates, and runners, such that a new object is instantiated
        // every time, to prevent leftover values from lingering in server mode.
        final MapBinder<String, ICommand> remoteCommands =
            MapBinder.newMapBinder(binder(), String.class, ICommand.class, Names.named("remote"));
        remoteCommands.addBinding("parse").to(RemoteParseCommand.class);
        remoteCommands.addBinding("analyze").to(RemoteAnalyzeCommand.class);
        remoteCommands.addBinding("transform").to(RemoteTransformCommand.class);
        remoteCommands.addBinding("build").to(RemoteBuildCommand.class);
        remoteCommands.addBinding("load").to(LoadLanguageCommand.class);

        bind(LanguagesDelegate.class);
        bind(ProjectPathDelegate.class);
        bind(InputDelegate.class);

        bind(Runner.class);
    }

    /**
     * Overrides {@link MetaborgModule#bindProject()} for non-dummy implementation of project service.
     */
    @Override protected void bindProject() {
        bind(SimpleProjectService.class).in(Singleton.class);
        bind(ISimpleProjectService.class).to(SimpleProjectService.class);
        bind(IProjectService.class).to(SimpleProjectService.class);
    }

    /**
     * Overrides {@link MetaborgModule#bindEditor()} for null implementation of editor registry.
     */
    @Override protected void bindEditor() {
        bind(IEditorRegistry.class).to(NullEditorRegistry.class).in(Singleton.class);
    }
}
