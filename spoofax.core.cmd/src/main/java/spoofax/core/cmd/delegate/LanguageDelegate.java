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

import com.beust.jcommander.Parameter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class LanguageDelegate {
    // @formatter:off
    @Parameter(names = { "-l", "--language" }, required = true,
        description = "Language to load. Can be an absolute path, or a relative path to the current directory") 
    public String path;
    // @formatter:on

    private final IResourceService resourceService;
    private final ILanguageDiscoveryService languageDiscoveryService;


    @Inject public LanguageDelegate(IResourceService resourceService,
        ILanguageDiscoveryService languageDiscoveryService) {
        this.resourceService = resourceService;
        this.languageDiscoveryService = languageDiscoveryService;
    }


    public Iterable<ILanguageComponent> discoverLanguages() throws MetaborgException {
        final FileObject location = resourceService.resolve(path);
        try {
            if(!location.exists()) {
                throw new MetaborgException("Cannot discover languages at " + path + ", it does not exist");
            }
        } catch(FileSystemException e) {
            throw new MetaborgException("Unable to check if " + location + " exists", e);
        }

        final Collection<ILanguageComponent> components = Lists.newLinkedList();
        Iterables.addAll(components, languageDiscoveryService.discover(languageDiscoveryService.request(location)));

        if(components.isEmpty()) {
            throw new MetaborgException("No languages were discovered");
        }

        return components;
    }

    public ILanguageImpl discoverLanguage() throws MetaborgException {
        final Iterable<ILanguageComponent> langComponents = discoverLanguages();
        final Iterable<ILanguageImpl> langImpls = LanguageUtils.toImpls(langComponents);
        final ILanguageImpl langImpl = Iterables.get(langImpls, 0);
        return langImpl;
    }
}
