package org.metaborg.sunshine.arguments;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
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
    @Parameter(names = { "-i", "--input" }, required = true, description = "Path to the input. Can be an absolute path, "
        + "or a relative path to the project if set, otherwise a relative path to the current directory") 
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
        final IdentifiedResource identified = languageIdentifierService.identifyToResource(resource, languages);
        if(identified == null) {
            final String message = String.format("Cannot not identify language of %s", resource);
            throw new MetaborgException(message);
        }
        return identified;
    }

    public FileObject inputResource(FileObject base) throws MetaborgException {
        try {
            return base.resolveFile(inputPath);
        } catch(FileSystemException e) {
            final String message = String.format("Cannot resolve %s", inputPath);
            throw new MetaborgException(message, e);
        }
    }

    public final IdentifiedResource
        inputIdentifiedResource(FileObject base, Iterable<? extends ILanguageImpl> languages) throws MetaborgException {
        final FileObject resource = inputResource(base);
        final IdentifiedResource identified = languageIdentifierService.identifyToResource(resource, languages);
        if(identified == null) {
            final String message = String.format("Cannot not identify language of %s", resource);
            throw new MetaborgException(message);
        }
        return identified;
    }
}
