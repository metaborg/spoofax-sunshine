/**
 * 
 */
package org.metaborg.sunshine.ant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.resources.FileResource;
import org.metaborg.sunshine.dependdriver.AggregatedResource;
import org.metaborg.sunshine.dependdriver.IActionableDependency;
import org.metaborg.sunshine.dependdriver.IResource;
import org.metaborg.sunshine.dependdriver.PathPatternResource;
import org.metaborg.sunshine.dependdriver.PathToPathDependency;
import org.metaborg.sunshine.environment.ServiceRegistry;
import org.metaborg.sunshine.services.language.LanguageService;

/**
 * @author vladvergu
 * 
 */
public class DependencyAntType {
	// <dependency of="syntax/*.sdf" on="syntax/*.sdf3" vialanguage="SDF3"
	// viatransformation="generate-sdf" />

	public DependencyAntType() {

	}

	Path of;
	Path on;
	Path enterat;
	String vialanguage;
	String viabuilder;
	List<WithArgAntType> args = new ArrayList<>();

	public Path getOf() {
		return of;
	}

	public void setOf(Path of) {
		this.of = of;
	}

	public Path getOn() {
		return on;
	}

	public void setOn(Path on) {
		this.on = on;
	}

	public String getVialanguage() {
		return vialanguage;
	}

	public void setVialanguage(String vialanguage) {
		this.vialanguage = vialanguage;
	}

	public String getViabuilder() {
		return viabuilder;
	}

	public void setViabuilder(String viatransformation) {
		this.viabuilder = viatransformation;
	}

	public void setEnterat(Path enterat) {
		this.enterat = enterat;
	}

	public Path getEnterat() {
		return enterat;
	}

	public void add(WithArgAntType arg) {
		this.args.add(arg);
	}

	public List<WithArgAntType> getArgs() {
		return args;
	}

	private static Collection<IResource> collectResourcesFrom(Path resource) {
		Project project = resource.getProject();
		java.nio.file.Path projectPath = project.getBaseDir().toPath()
				.toAbsolutePath();
		Collection<IResource> resources = new LinkedList<>();
		for (Resource resource_fragment : resource) {
			FileResource fileresource_fragment = (FileResource) resource_fragment;
			if (!fileresource_fragment.getFile().toPath().toAbsolutePath()
					.startsWith(projectPath)) {
				throw new BuildException("Resource " + resource_fragment
						+ " is not part of project " + projectPath);
			}
			// String pathPattern = projectPath.relativize(
			// fileresource_fragment.getFile().toPath()).toString();
			String pathPattern = fileresource_fragment.getFile().toPath()
					.toAbsolutePath().toString();
			resources.add(new PathPatternResource(projectPath.toAbsolutePath(),
					pathPattern));
		}
		return resources;
	}

	public IActionableDependency toActionableDependency() {
		IResource ofResource = new AggregatedResource(
				collectResourcesFrom(getOf()));
		IResource onResource = new AggregatedResource(
				collectResourcesFrom(getOn()));
		IResource entryAtResource = null;
		if (getEnterat() == null) {
			entryAtResource = onResource;
		} else {
			entryAtResource = new AggregatedResource(
					collectResourcesFrom(getEnterat()));
		}
		return new PathToPathDependency(onResource, ofResource,
				entryAtResource, ServiceRegistry.INSTANCE()
						.getService(LanguageService.class)
						.getLanguageByName(getVialanguage()), getViabuilder());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("<dependency of=\"");
		builder.append(of);
		builder.append("\" on=\"");
		builder.append(on);
		builder.append("\" enterat=\"");
		builder.append(enterat);
		builder.append("\" vialanguage=\"");
		builder.append(vialanguage);
		builder.append("\" viabuilder=\"");
		builder.append(viabuilder);
		builder.append("\">\n");
		for (WithArgAntType arg : args) {
			builder.append("\t" + arg + "\n");
		}
		builder.append("</dependency>\n");
		return builder.toString();
	}

}
