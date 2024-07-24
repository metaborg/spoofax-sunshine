package spoofax.core.cmd.delegate;

import java.util.Arrays;

import org.metaborg.core.language.ILanguageDiscoveryService;
import org.metaborg.core.resource.IResourceService;

import com.beust.jcommander.Parameter;

public class LanguageDelegate extends ALanguageLoader {
    // @formatter:off
    @Parameter(names = { "-l", "--language" }, required = true,
        description = "Language to load. Can be an absolute path, or a relative path to the current directory") 
    public String path;
    // @formatter:on


    @jakarta.inject.Inject public LanguageDelegate(IResourceService resourceService,
        ILanguageDiscoveryService languageDiscoveryService) {
        super(resourceService, languageDiscoveryService);
    }


    @Override protected Iterable<String> paths() {
        return Arrays.asList(path);
    }
}
