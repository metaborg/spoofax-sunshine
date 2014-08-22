/**
 * 
 */
package org.metaborg.sunshine;

import java.io.IOException;

import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.sunshine.environment.LaunchConfiguration;
import org.metaborg.sunshine.environment.ServiceRegistry;
import org.spoofax.interpreter.library.LoggingIOAgent;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class SunshineIOAgent extends LoggingIOAgent {

	private final ILanguage language;

	public SunshineIOAgent(ILanguage language) {
		this.language = language;
		LaunchConfiguration launch = ServiceRegistry.INSTANCE().getService(
				LaunchConfiguration.class);
		try {
			if (launch.projectDir != null) {
				this.setWorkingDir(launch.projectDir.getAbsolutePath());
			}
			// GTODO: does this produce a path string that the IOAgent accepts?
			this.setDefinitionDir(language.location().getName()
					.getPathDecoded());
		} catch (IOException ioex) {
			throw new RuntimeException("Failed to create IOAgent", ioex);
		}
	}

	public ILanguage getLanguage() {
		return language;
	}
}
