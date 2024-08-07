package spoofax.core.cmd.delegate;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.ISimpleProjectService;
import org.metaborg.core.resource.IResourceService;

import com.beust.jcommander.Parameter;

public class ProjectPathDelegate {
    // @formatter:off
    @Parameter(names = { "-p", "--project" }, required = true, description = "Path to the project. "
        + "Can be an absolute path, or a relative path to the current directory")
    private String projectPath;
    // @formatter:on

    private final IResourceService resourceService;
    private final ISimpleProjectService projectService;

    private IProject project;


    @jakarta.inject.Inject public ProjectPathDelegate(IResourceService resourceService, ISimpleProjectService projectService) {
        this.resourceService = resourceService;
        this.projectService = projectService;
    }


    public FileObject projectLocation() throws MetaborgException {
        try {
            return resourceService.resolve(projectPath);
        } catch(MetaborgRuntimeException e) {
            throw new MetaborgException("Cannot resolve " + projectPath, e);
        }
    }

    public IProject project() throws MetaborgException {
        if(project == null) {
            final FileObject location = projectLocation();
            project = projectService.create(location);
        }
        return project;
    }

    public void removeProject() throws MetaborgException {
        if(project != null) {
            projectService.remove(project);
            project = null;
        }
    }
}
