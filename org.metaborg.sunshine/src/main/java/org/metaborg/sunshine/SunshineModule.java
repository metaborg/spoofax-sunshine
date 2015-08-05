package org.metaborg.sunshine;

import org.metaborg.core.MetaborgModule;
import org.metaborg.core.editor.IEditorRegistry;
import org.metaborg.core.editor.NullEditorRegistry;
import org.metaborg.core.project.IProjectService;
import org.metaborg.core.project.ISingleProjectService;
import org.metaborg.core.project.SingleProjectService;
import org.metaborg.spoofax.core.SpoofaxModule;
import org.metaborg.spoofax.core.project.IMavenProjectService;
import org.metaborg.spoofax.core.project.NullMavenProjectService;
import org.metaborg.sunshine.command.AnalyzeCommand;
import org.metaborg.sunshine.command.BuildCommand;
import org.metaborg.sunshine.command.ICommand;
import org.metaborg.sunshine.command.ParseCommand;
import org.metaborg.sunshine.command.TransformCommand;
import org.metaborg.sunshine.command.arguments.CommonArguments;
import org.metaborg.sunshine.command.arguments.InputDelegate;
import org.metaborg.sunshine.command.arguments.ProjectPathDelegate;

import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;

public class SunshineModule extends SpoofaxModule {
    @Override protected void configure() {
        super.configure();

        final MapBinder<String, ICommand> commands = MapBinder.newMapBinder(binder(), String.class, ICommand.class);
        commands.addBinding("parse").to(ParseCommand.class).in(Singleton.class);
        commands.addBinding("analyze").to(AnalyzeCommand.class).in(Singleton.class);
        commands.addBinding("transform").to(TransformCommand.class).in(Singleton.class);
        commands.addBinding("build").to(BuildCommand.class).in(Singleton.class);

        bind(CommonArguments.class).in(Singleton.class);
        bind(ProjectPathDelegate.class).in(Singleton.class);
        bind(InputDelegate.class).in(Singleton.class);

        bind(Main.class).in(Singleton.class);
    }

    /**
     * Overrides {@link MetaborgModule#bindProject()} for non-dummy implementation of project service.
     */
    @Override protected void bindProject() {
        bind(SingleProjectService.class).in(Singleton.class);
        bind(ISingleProjectService.class).to(SingleProjectService.class);
        bind(IProjectService.class).to(SingleProjectService.class);
    }

    /**
     * Overrides {@link SpoofaxModule#bindMavenProject()} for null implementation of Maven project service.
     */
    @Override protected void bindMavenProject() {
        bind(IMavenProjectService.class).to(NullMavenProjectService.class).in(Singleton.class);
    }

    /**
     * Overrides {@link MetaborgModule#bindEditor()} for null implementation of editor registry.
     */
    @Override protected void bindEditor() {
        bind(IEditorRegistry.class).to(NullEditorRegistry.class).in(Singleton.class);
    }
}
