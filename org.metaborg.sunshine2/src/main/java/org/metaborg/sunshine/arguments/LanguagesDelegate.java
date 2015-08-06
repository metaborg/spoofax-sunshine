package org.metaborg.sunshine.arguments;

import java.util.List;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.sunshine.common.LanguageLoader;

import com.beust.jcommander.Parameter;
import com.google.inject.Inject;

public class LanguagesDelegate {
    // @formatter:off
    @Parameter(names = { "-l", "--language" }, required = true,
        description = "Paths to discover and load languages at. Can be an absolute path, "
            + "or a relative path to the current directory") 
    public List<String> languageDiscoveryPaths;
    // @formatter:on

    private final LanguageLoader languageLoader;


    @Inject public LanguagesDelegate(LanguageLoader languageLoader) {
        this.languageLoader = languageLoader;
    }


    public Iterable<ILanguageComponent> discoverLanguages() throws MetaborgException {
        return languageLoader.discoverLanguages(languageDiscoveryPaths);
    }
}
