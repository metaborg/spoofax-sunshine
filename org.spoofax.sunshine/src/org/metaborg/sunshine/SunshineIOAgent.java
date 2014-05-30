/**
 * 
 */
package org.metaborg.sunshine;

import java.io.IOException;

import org.metaborg.sunshine.environment.LaunchConfiguration;
import org.metaborg.sunshine.environment.ServiceRegistry;
import org.metaborg.sunshine.services.language.ALanguage;
import org.spoofax.interpreter.library.LoggingIOAgent;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class SunshineIOAgent extends LoggingIOAgent {

	private final ALanguage language;

	public SunshineIOAgent(ALanguage language) {
		this.language = language;
		LaunchConfiguration launch = ServiceRegistry.INSTANCE().getService(
				LaunchConfiguration.class);
		try {
			if (launch.projectDir != null) {
				this.setWorkingDir(launch.projectDir.getAbsolutePath());
			}
			this.setDefinitionDir(language.getDefinitionPath().toAbsolutePath().toString());
		} catch (IOException ioex) {
			throw new RuntimeException("Failed to create IOAgent", ioex);
		}
	}

	public ALanguage getLanguage() {
		return language;
	}
}
