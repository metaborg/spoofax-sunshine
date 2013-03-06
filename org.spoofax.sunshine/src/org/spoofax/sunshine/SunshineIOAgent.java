/**
 * 
 */
package org.spoofax.sunshine;

import org.spoofax.interpreter.library.LoggingIOAgent;
import org.spoofax.sunshine.framework.language.ALanguage;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 *
 */
public class SunshineIOAgent extends LoggingIOAgent {

	private ALanguage language;
	
	public void setLanguage(ALanguage language) {
		this.language = language;
	}
	
	public ALanguage getLanguage() {
		return language;
	}
}
