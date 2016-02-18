package org.metaborg.sunshine.command.remote;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.build.dependency.IDependencyService;
import org.metaborg.core.build.paths.ILanguagePathService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.ILanguageService;
import org.metaborg.core.language.LanguageUtils;
import org.metaborg.core.source.ISourceTextService;
import org.metaborg.spoofax.core.processing.ISpoofaxProcessorRunner;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.sunshine.arguments.InputDelegate;
import org.metaborg.sunshine.arguments.ProjectPathDelegate;
import org.metaborg.sunshine.command.base.AnalyzeCommand;

import com.google.inject.Inject;

public class RemoteAnalyzeCommand extends AnalyzeCommand {
    private final ILanguageService languageService;


    @Inject public RemoteAnalyzeCommand(ISourceTextService sourceTextService, IDependencyService dependencyService,
                                        ILanguagePathService languagePathService, ISpoofaxProcessorRunner runner, IStrategoCommon strategoCommon,
                                        ProjectPathDelegate languageSpecPathDelegate, InputDelegate inputDelegate, ILanguageService languageService) {
        super(sourceTextService, dependencyService, languagePathService, runner, strategoCommon, languageSpecPathDelegate,
            inputDelegate);
        this.languageService = languageService;
    }


    @Override public int run() throws MetaborgException {
        final Iterable<ILanguageImpl> activeImpls = LanguageUtils.allActiveImpls(languageService);
        return run(activeImpls);
    }
}
