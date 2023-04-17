package spoofax.core.cmd.delegate;

import java.util.Collection;
import java.util.LinkedList;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageDiscoveryService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.LanguageUtils;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.util.iterators.Iterables2;

public abstract class ALanguageLoader {
    private final IResourceService resourceService;
    private final ILanguageDiscoveryService languageDiscoveryService;


    public ALanguageLoader(IResourceService resourceService, ILanguageDiscoveryService languageDiscoveryService) {
        this.resourceService = resourceService;
        this.languageDiscoveryService = languageDiscoveryService;
    }


    protected abstract Iterable<String> paths();


    public Iterable<ILanguageComponent> discoverComponents() throws MetaborgException {
        final Collection<ILanguageComponent> components = new LinkedList<>();
        for(String path : paths()) {
            final FileObject location = resourceService.resolve(path);
            try {
                if(!location.exists()) {
                    throw new MetaborgException("Cannot discover languages at " + path + ", it does not exist");
                }
            } catch(FileSystemException e) {
                throw new MetaborgException("Unable to check if " + location + " exists", e);
            }

            Iterables2.addAll(components, languageDiscoveryService.discover(languageDiscoveryService.request(location)));

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
        if(Iterables2.size(langImpls) > 1) {
            throw new MetaborgException(
                "Multiple language implementations were loaded, while a single language implementation was expected");
        }
        final ILanguageImpl langImpl = langImpls.iterator().next();
        return langImpl;
    }
}
