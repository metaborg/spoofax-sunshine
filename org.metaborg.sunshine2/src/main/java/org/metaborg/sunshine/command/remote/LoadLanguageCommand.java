package org.metaborg.sunshine.command.remote;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.LanguageUtils;
import org.metaborg.sunshine.arguments.LanguagesDelegate;
import org.metaborg.util.iterators.Iterables2;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;

import spoofax.core.cmd.command.ICommand;

@Parameters(commandDescription = "Discovers and loads languages")
public class LoadLanguageCommand implements ICommand {
    private static final ILogger logger = LoggerUtils.logger(LoadLanguageCommand.class);

    @ParametersDelegate private LanguagesDelegate languagesDelegate;


    @jakarta.inject.Inject @javax.inject.Inject public LoadLanguageCommand(LanguagesDelegate languagesDelegate) {
        this.languagesDelegate = languagesDelegate;
    }


    @Override public boolean validate() {
        return true;
    }

    @Override public int run() throws MetaborgException {
        final Iterable<ILanguageComponent> components = languagesDelegate.discoverLanguages();
        final Iterable<ILanguageImpl> impls = LanguageUtils.toImpls(components);

        logger.info("Discovered {} language component(s): ", Iterables2.size(components));
        for(ILanguageComponent component : components) {
            logger.info("  {}", component.toString());
        }

        logger.info("Belonging to {} language implementation(s): ", Iterables2.size(impls));
        for(ILanguageImpl impl : impls) {
            logger.info("  {}", impl.toString());
        }

        return 0;
    }
}
