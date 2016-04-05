package org.metaborg.sunshine.command.remote;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.build.dependency.IDependencyService;
import org.metaborg.core.build.paths.ILanguagePathService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.ILanguageService;
import org.metaborg.core.language.LanguageUtils;
import org.metaborg.core.source.ISourceTextService;
import org.metaborg.spoofax.core.processing.ISpoofaxProcessorRunner;
import org.metaborg.sunshine.arguments.ProjectPathDelegate;
import org.metaborg.sunshine.command.base.BuildCommand;

import com.google.inject.Inject;

public class RemoteBuildCommand extends BuildCommand {
    private final ILanguageService languageService;


    @Inject public RemoteBuildCommand(ISourceTextService sourceTextService, IDependencyService dependencyService,
                                      ILanguagePathService languagePathService, ISpoofaxProcessorRunner runner,
                                      ProjectPathDelegate languageSpecPathDelegate, ILanguageService languageService) {
        super(sourceTextService, dependencyService, languagePathService, runner, languageSpecPathDelegate);
        this.languageService = languageService;
    }


    @Override public int run() throws MetaborgException {
        final Iterable<ILanguageImpl> activeImpls = LanguageUtils.allActiveImpls(languageService);
        return run(activeImpls);
    }
}
