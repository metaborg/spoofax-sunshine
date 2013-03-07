/**
 * 
 */
package org.spoofax.sunshine.framework.services;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.spoofax.sunshine.framework.language.ALanguage;

/**
 * Singleton service serving the purpose of {@link ALanguage} registry. This service maintains
 * references to languages which are accessible by file extension ({@link #getLanguageByExten(File)}
 * ) or by name ({@link #getLanguageByName(String)}). New languages can be registered by calls to
 * {@link #registerLanguage(ALanguage)}.
 * 
 * or by name (
 * 
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class LanguageService {

	private static LanguageService INSTANCE;
	private final Map<String, ALanguage> exten2lang = new HashMap<String, ALanguage>();
	private final Map<String, ALanguage> name2lang = new HashMap<String, ALanguage>();

	private LanguageService() {
	}

	public static final LanguageService INSTANCE() {
		if (INSTANCE == null) {
			INSTANCE = new LanguageService();
		}
		return INSTANCE;
	}

	/**
	 * Register a new {@link ALanguage} in this registry. Languages are registered by file extension
	 * and by name. Thus if multiple calls to this method with different languages with the same
	 * name are made then only the last registered language will be accessible via this registry.
	 * The same applies to file extensions, thus if multiple registered languages handle the same
	 * extension only the last registered one will be accessible.
	 * 
	 * @param lang
	 *            The {@link ALanguage} to register
	 */
	public void registerLanguage(ALanguage lang) {
		name2lang.put(lang.getName(), lang);
		for (String exten : lang.getFileExtensions()) {
			exten2lang.put(exten, lang);
		}
	}

	/**
	 * Lookup an {@link ALanguage} by its name.
	 * 
	 * @param name
	 *            The name of the language to retrieve from the registry.
	 * @return An instance of {@link ALanguage} if a language with the given name is registered,
	 *         <code>null</code> otherwise.
	 */
	public ALanguage getLanguageByName(String name) {
		return name2lang.get(name);
	}

	/**
	 * Lookup an {@link ALanguage} that handles files with the given extension.
	 * 
	 * @param exten
	 *            A file extension, for example <code>".foo"</code>.
	 * @return An instance of {@link ALanguage} if a language handling the give extension is
	 *         registered, <code>null</code> otherwise.
	 */
	public ALanguage getLanguageByExten(String exten) {
		return exten2lang.get(exten);
	}

	/**
	 * @see #getLanguageByExten(String)
	 */
	public ALanguage getLanguageByExten(File file) {
		return getLanguageByExten(FilenameUtils.getExtension(file.getName()));
	}

}
