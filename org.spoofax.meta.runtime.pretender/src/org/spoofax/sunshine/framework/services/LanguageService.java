/**
 * 
 */
package org.spoofax.sunshine.framework.services;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.spoofax.sunshine.framework.language.ILanguage;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class LanguageService {

	private static LanguageService INSTANCE;
	private final Map<String, ILanguage> exten2lang = new HashMap<String, ILanguage>();
	private final Map<String, ILanguage> name2lang = new HashMap<String, ILanguage>();

	private LanguageService() {
	}

	public static final LanguageService INSTANCE() {
		if (INSTANCE == null) {
			INSTANCE = new LanguageService();
		}
		return INSTANCE;
	}

	public void registerLanguage(ILanguage lang) {
		name2lang.put(lang.getName(), lang);
		for (String exten : lang.getFileExtensions()) {
			exten2lang.put(exten, lang);
		}
	}
	
	public ILanguage getLanguageByName(String name) {
		return name2lang.get(name);
	}

	public ILanguage getLanguageByExten(String exten) {
		return exten2lang.get(exten);
	}

	public ILanguage getLanguageByExten(File file) {
		return getLanguageByExten("." + FilenameUtils.getExtension(file.getName()));
	}

}
