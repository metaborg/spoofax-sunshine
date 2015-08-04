package org.metaborg.sunshine.command;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.SingleProjectService;
import org.metaborg.core.resource.IResourceService;

import com.beust.jcommander.Parameter;
import com.google.inject.Inject;

public class ProjectPathDelegate {
    // @formatter:off
    @Parameter(names = { "-p", "--project" }, required = true, description = "Absolute or relative to current directory path where project resides") 
    private String projectPath;
    // @formatter:on


    private final IResourceService resourceService;
    private final SingleProjectService projectService;

    @Inject public ProjectPathDelegate(IResourceService resourceService, SingleProjectService projectService) {
        this.resourceService = resourceService;
        this.projectService = projectService;
    }


    public FileObject projectLocation() throws MetaborgException {
        try {
            return resourceService.resolve(projectPath);
        } catch(MetaborgRuntimeException e) {
            final String message = String.format("Cannot resolve %s", projectPath);
            throw new MetaborgException(message, e);
        }
    }

    public IProject project() throws MetaborgException {
        final FileObject location = projectLocation();
        final IProject project = new IProject() {
            @Override public FileObject location() {
                return location;
            }
        };
        projectService.set(project);
        return project;
    }
}
