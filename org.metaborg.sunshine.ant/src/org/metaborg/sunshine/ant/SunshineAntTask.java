/**
 * 
 */
package org.metaborg.sunshine.ant;

import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Resource;

/**
 * @author vladvergu
 * 
 */
public class SunshineAntTask extends Task {

	Path languageRepository;

	List<DependencyAntType> dependencies = new ArrayList<>();

	public void setLanguagerepository(Path languageRepository) {
		this.languageRepository = languageRepository;
	}

	public void add(DependencyAntType dependency) {
		this.dependencies.add(dependency);
	}

	@Override
	public void execute() throws BuildException {

		System.out.println("Here's a path " + languageRepository);
		System.out.println("And i have " + dependencies.size()
				+ " dependencies");
		for (DependencyAntType dep : dependencies) {
			System.out.println(dep);
			for (Resource resource : dep.getOf()) {
				System.out.println("LOCO " + resource.toLongString());
			}
		}
	}

}
