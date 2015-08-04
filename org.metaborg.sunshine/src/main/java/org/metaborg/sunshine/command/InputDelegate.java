package org.metaborg.sunshine.command;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.language.ILanguageIdentifierService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.IdentifiedResource;
import org.metaborg.core.resource.IResourceService;

import com.beust.jcommander.Parameter;
import com.google.inject.Inject;

public class InputDelegate {
    // @formatter:off
    @Parameter(names = { "-i", "--input" }, required = true, description = "Absolute or relative to current directory path of the input") 
    private String inputPath;
    // @formatter:on


    private final IResourceService resourceService;
    private final ILanguageIdentifierService languageIdentifierService;


    @Inject public InputDelegate(IResourceService resourceService, ILanguageIdentifierService languageIdentifierService) {
        this.resourceService = resourceService;
        this.languageIdentifierService = languageIdentifierService;
    }


    public FileObject inputResource() throws MetaborgException {
        try {
            return resourceService.resolve(inputPath);
        } catch(MetaborgRuntimeException e) {
            final String message = String.format("Cannot resolve %s", inputPath);
            throw new MetaborgException(message, e);
        }
    }

    public final IdentifiedResource inputIdentifiedResource(Iterable<? extends ILanguageImpl> languages)
        throws MetaborgException {
        final FileObject resource = inputResource();
        return languageIdentifierService.identifyToResource(resource, languages);
    }
}
