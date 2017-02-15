package spoofax.core.cmd.delegate;

import java.util.List;

import org.metaborg.core.language.ILanguageDiscoveryService;
import org.metaborg.core.resource.IResourceService;

import com.beust.jcommander.Parameter;
import com.google.inject.Inject;

public class LanguagesDelegate extends ALanguageLoader {
    // @formatter:off
    @Parameter(names = { "-l", "--language" }, required = true,
        description = "Languages to load. Can be an absolute path, or a relative path to the current directory") 
    public List<String> paths;
    // @formatter:on


    @Inject public LanguagesDelegate(IResourceService resourceService,
        ILanguageDiscoveryService languageDiscoveryService) {
        super(resourceService, languageDiscoveryService);
    }


    @Override protected Iterable<String> paths() {
        return paths;
    }
}
