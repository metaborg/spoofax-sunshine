package spoofax.core.cmd.delegate;

import java.util.Collection;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageDiscoveryService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.LanguageUtils;
import org.metaborg.core.resource.IResourceService;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public abstract class ALanguageLoader {
    private final IResourceService resourceService;
    private final ILanguageDiscoveryService languageDiscoveryService;


    public ALanguageLoader(IResourceService resourceService, ILanguageDiscoveryService languageDiscoveryService) {
        this.resourceService = resourceService;
        this.languageDiscoveryService = languageDiscoveryService;
    }


    protected abstract Iterable<String> paths();


    public Iterable<ILanguageComponent> discoverComponents() throws MetaborgException {
        final Collection<ILanguageComponent> components = Lists.newLinkedList();
        for(String path : paths()) {
            final FileObject location = resourceService.resolve(path);
            try {
                if(!location.exists()) {
                    throw new MetaborgException("Cannot discover languages at " + path + ", it does not exist");
                }
            } catch(FileSystemException e) {
                throw new MetaborgException("Unable to check if " + location + " exists", e);
            }

            Iterables.addAll(components, languageDiscoveryService.discover(languageDiscoveryService.request(location)));

            if(components.isEmpty()) {
                throw new MetaborgException("No languages were discovered");
            }
        }

        return components;
    }

    public Iterable<ILanguageImpl> discoverLanguages() throws MetaborgException {
        return LanguageUtils.toImpls(discoverComponents());
    }

    public ILanguageImpl discoverLanguage() throws MetaborgException {
        final Iterable<ILanguageComponent> langComponents = discoverComponents();
        final Iterable<ILanguageImpl> langImpls = LanguageUtils.toImpls(langComponents);
        if(Iterables.size(langImpls) > 1) {
            throw new MetaborgException(
                "Multiple language implementations were loaded, while a single language implementation was expected");
        }
        final ILanguageImpl langImpl = Iterables.get(langImpls, 0);
        return langImpl;
    }
}
