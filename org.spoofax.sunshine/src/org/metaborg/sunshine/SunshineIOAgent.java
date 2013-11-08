/**
 * 
 */
package org.metaborg.sunshine;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.metaborg.sunshine.services.language.ALanguage;
import org.spoofax.interpreter.library.LoggingIOAgent;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class SunshineIOAgent extends LoggingIOAgent {

	private ALanguage language;

	public SunshineIOAgent() {
		try {
			this.setWorkingDir(Environment.INSTANCE().projectDir.getAbsolutePath());
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void setLanguage(ALanguage language) {
		this.language = language;
	}

	public ALanguage getLanguage() {
		return language;
	}
}
