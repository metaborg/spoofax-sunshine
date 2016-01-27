package org.metaborg.sunshine.common;

import java.util.Collection;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageDiscoveryService;
import org.metaborg.core.resource.IResourceService;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class LanguageLoader {
    private final IResourceService resourceService;
    private final ILanguageDiscoveryService languageDiscoveryService;


    @Inject public LanguageLoader(IResourceService resourceService, ILanguageDiscoveryService languageDiscoveryService) {
        this.resourceService = resourceService;
        this.languageDiscoveryService = languageDiscoveryService;
    }


    public Iterable<ILanguageComponent> discoverLanguages(Iterable<String> paths) throws MetaborgException {
        final Collection<FileObject> languageLocations = Lists.newLinkedList();
        try {
            for(String path : paths) {
                final FileObject location = resourceService.resolve(path);
                if(!location.exists()) {
                    final String message = String.format("Cannot discover languages at %s, it does not exist", path);
                    throw new MetaborgException(message);
                }
                languageLocations.add(location);
            }
        } catch(Exception e) {
            final String message = "Discovering languages failed unexpectedly";
            throw new MetaborgException(message, e);
        }

        final Collection<ILanguageComponent> components = Lists.newLinkedList();
        for(FileObject location : languageLocations) {
            Iterables.addAll(components, languageDiscoveryService.discover(languageDiscoveryService.request(location)));
        }

        if(components.isEmpty()) {
            throw new MetaborgException("No languages were discovered");
        }

        return components;
    }
}
