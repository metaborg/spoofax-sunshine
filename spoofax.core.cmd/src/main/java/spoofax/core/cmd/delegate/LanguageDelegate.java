package spoofax.core.cmd.delegate;

import org.metaborg.core.language.ILanguageDiscoveryService;
import org.metaborg.core.resource.IResourceService;

import com.beust.jcommander.Parameter;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class LanguageDelegate extends ALanguageLoader {
    // @formatter:off
    @Parameter(names = { "-l", "--language" }, required = true,
        description = "Language to load. Can be an absolute path, or a relative path to the current directory") 
    public String path;
    // @formatter:on


    @Inject public LanguageDelegate(IResourceService resourceService,
        ILanguageDiscoveryService languageDiscoveryService) {
        super(resourceService, languageDiscoveryService);
    }


    @Override protected Iterable<String> paths() {
        return Lists.newArrayList(path);
    }
}
