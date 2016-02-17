package org.metaborg.sunshine.arguments;

import com.beust.jcommander.Parameter;
import com.google.inject.Inject;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.ISimpleProjectService;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.meta.core.project.ILanguageSpec;
import org.metaborg.meta.core.project.ILanguageSpecService;

public class LanguageSpecPathDelegate {
    // @formatter:off
    @Parameter(names = { "-p", "--project" }, required = true, description = "Path to the project. "
        + "Can be an absolute path, or a relative path to the current directory")
    private String projectPath;
    // @formatter:on

    private final IResourceService resourceService;
    private final ISimpleProjectService projectService;
    private final ILanguageSpecService languageSpecService;

    private IProject project;
    private ILanguageSpec languageSpec;


    @Inject public LanguageSpecPathDelegate(IResourceService resourceService, ISimpleProjectService projectService, ILanguageSpecService languageSpecService) {
        this.resourceService = resourceService;
        this.projectService = projectService;
        this.languageSpecService = languageSpecService;
    }


    public FileObject projectLocation() throws MetaborgException {
        try {
            return resourceService.resolve(projectPath);
        } catch(MetaborgRuntimeException e) {
            final String message = String.format("Cannot resolve %s", projectPath);
            throw new MetaborgException(message, e);
        }
    }

    private IProject project() throws MetaborgException {
        if(this.project == null) {
            final FileObject location = projectLocation();
            this.project = projectService.create(location);
        }
        return this.project;
    }

    public ILanguageSpec languageSpec() throws MetaborgException {
        if (this.languageSpec == null) {
            this.languageSpec = this.languageSpecService.get(project());
        }
        return this.languageSpec;
    }

    public void removeProject() throws MetaborgException {
        if(this.project != null) {
            projectService.remove(this.project);
            this.project = null;
            this.languageSpec = null;
        }
    }
}
