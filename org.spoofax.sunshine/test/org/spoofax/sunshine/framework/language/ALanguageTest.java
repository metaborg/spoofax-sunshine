/**
 * 
 */
package org.spoofax.sunshine.framework.language;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spoofax.sunshine.language.LanguageNature;
import org.spoofax.sunshine.parser.model.IParseTableProvider;
import org.spoofax.sunshine.services.language.ALanguage;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class ALanguageTest {

	ALanguage lang;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		lang = new ALanguage("foolang", LanguageNature.JAR_NATURE) {

			@Override
			public String getStartSymbol() {
				return null;
			}

			@Override
			public IParseTableProvider getParseTableProvider() {
				return null;
			}

			@Override
			public Collection<String> getFileExtensions() {
				return null;
			}

			@Override
			public File[] getCompilerFiles() {
				return null;
			}

			@Override
			public String getAnalysisFunction() {
				return null;
			}
		};
	}

	@After
	public void tearDown() throws Exception {
		lang = null;
	}

	/**
	 * Test method for {@link org.spoofax.sunshine.services.language.ALanguage#getName()}.
	 */
	@Test
	public void testGetName() {
		assertEquals("foolang", lang.getName());
	}

	/**
	 * Test method for {@link org.spoofax.sunshine.services.language.ALanguage#getNature()}.
	 */
	@Test
	public void testGetNature() {
		assertEquals(LanguageNature.JAR_NATURE, lang.getNature());
	}

}
