/**
 * 
 */
package org.spoofax.sunshine.framework.language;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spoofax.sunshine.model.language.ALanguage;
import org.spoofax.sunshine.model.language.Language;
import org.spoofax.sunshine.services.parser.FileBasedParseTableProvider;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class AdHocJarBasedLanguageTest {

	ALanguage lang;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		lang = new Language("foolang", new String[] { ".txt", ".foo" }, "StartS",
				new File("hello"), "some-analysis", new File("jar.jar"));
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		lang = null;
	}

	/**
	 * Test method for {@link org.spoofax.sunshine.model.language.Language#getFileExtensions()} .
	 */
	@Test
	public void testGetFileExtensions() {
		final Collection<String> extens = lang.getFileExtensions();
		assertTrue(extens.contains(".txt"));
		assertTrue(extens.contains(".foo"));
		assertFalse(extens.contains(".bar"));
		assertEquals(2, extens.size());
	}

	/**
	 * Test method for {@link org.spoofax.sunshine.model.language.Language#getStartSymbol()} .
	 */
	@Test
	public void testGetStartSymbol() {
		assertEquals("StartS", lang.getStartSymbol());
	}

	/**
	 * Test method for {@link org.spoofax.sunshine.model.language.Language#getParseTableProvider()}
	 * .
	 */
	@Test
	public void testGetParseTableProvider() {
		assertNotNull(lang.getParseTableProvider());
		assertTrue(lang.getParseTableProvider() instanceof FileBasedParseTableProvider);
	}

	/**
	 * Test method for {@link org.spoofax.sunshine.model.language.Language#getCompilerFiles()} .
	 */
	@Test
	public void testGetCompilerFiles() {
		final File[] files = lang.getCompilerFiles();
		assertEquals(1, files.length);
		assertEquals(new File("jar.jar"), files[0]);
	}

	/**
	 * Test method for {@link org.spoofax.sunshine.model.language.Language#getAnalysisFunction()} .
	 */
	@Test
	public void testGetAnalysisFunction() {
		assertEquals("some-analysis", lang.getAnalysisFunction());
	}

	/**
	 * Test method for
	 * {@link org.spoofax.sunshine.model.language.Language#AdHocJarBasedLanguage(java.lang.String, java.lang.String[], java.lang.String, java.io.File, java.lang.String, java.io.File)}
	 * .
	 */
	@Test
	public void testAdHocJarBasedLanguage1() {
		new Language("foolang", new String[] { ".txt", ".foo" }, "StartS", new File("hello"),
				"some-analysis", new File("jar.jar"));
	}

	/**
	 * Test method for
	 * {@link org.spoofax.sunshine.model.language.Language#AdHocJarBasedLanguage(java.lang.String, java.lang.String[], java.lang.String, java.io.File, java.lang.String, java.io.File)}
	 * .
	 */
	@Test(expected = AssertionError.class)
	public void testAdHocJarBasedLanguage2() {
		new Language(null, new String[] { ".txt", ".foo" }, "StartS", new File("hello"),
				"some-analysis", new File("jar.jar"));
	}

	/**
	 * Test method for
	 * {@link org.spoofax.sunshine.model.language.Language#AdHocJarBasedLanguage(java.lang.String, java.lang.String[], java.lang.String, java.io.File, java.lang.String, java.io.File)}
	 * .
	 */
	@Test(expected = AssertionError.class)
	public void testAdHocJarBasedLanguage3() {
		new Language("foolang", null, "StartS", new File("hello"), "some-analysis", new File(
				"jar.jar"));
	}

	/**
	 * Test method for
	 * {@link org.spoofax.sunshine.model.language.Language#AdHocJarBasedLanguage(java.lang.String, java.lang.String[], java.lang.String, java.io.File, java.lang.String, java.io.File)}
	 * .
	 */
	@Test(expected = AssertionError.class)
	public void testAdHocJarBasedLanguage4() {
		new Language("foolang", new String[0], "StartS", new File("hello"), "some-analysis",
				new File("jar.jar"));
	}

	/**
	 * Test method for
	 * {@link org.spoofax.sunshine.model.language.Language#AdHocJarBasedLanguage(java.lang.String, java.lang.String[], java.lang.String, java.io.File, java.lang.String, java.io.File)}
	 * .
	 */
	@Test(expected = AssertionError.class)
	public void testAdHocJarBasedLanguage5() {
		new Language("foolang", new String[] { ".txt", ".foo" }, null, new File("hello"),
				"some-analysis", new File("jar.jar"));
	}

	/**
	 * Test method for
	 * {@link org.spoofax.sunshine.model.language.Language#AdHocJarBasedLanguage(java.lang.String, java.lang.String[], java.lang.String, java.io.File, java.lang.String, java.io.File)}
	 * .
	 */
	@Test(expected = AssertionError.class)
	public void testAdHocJarBasedLanguage6() {
		new Language("foolang", new String[] { ".txt", ".foo" }, "", new File("hello"),
				"some-analysis", new File("jar"));
	}

	/**
	 * Test method for
	 * {@link org.spoofax.sunshine.model.language.Language#AdHocJarBasedLanguage(java.lang.String, java.lang.String[], java.lang.String, java.io.File, java.lang.String, java.io.File)}
	 * .
	 */
	@Test(expected = AssertionError.class)
	public void testAdHocJarBasedLanguage7() {
		new Language("foolang", new String[] { ".txt", ".foo" }, "StartS", null, "some-analysis",
				new File("jar.jar"));
	}

	/**
	 * Test method for
	 * {@link org.spoofax.sunshine.model.language.Language#AdHocJarBasedLanguage(java.lang.String, java.lang.String[], java.lang.String, java.io.File, java.lang.String, java.io.File)}
	 * .
	 */
	@Test(expected = AssertionError.class)
	public void testAdHocJarBasedLanguage8() {
		new Language("foolang", new String[] { ".txt", ".foo" }, "StartS", new File("hello"), null,
				new File("jar.jar"));
	}

	/**
	 * Test method for
	 * {@link org.spoofax.sunshine.model.language.Language#AdHocJarBasedLanguage(java.lang.String, java.lang.String[], java.lang.String, java.io.File, java.lang.String, java.io.File)}
	 * .
	 */
	@Test(expected = AssertionError.class)
	public void testAdHocJarBasedLanguage9() {
		new Language("foolang", new String[] { ".txt", ".foo" }, "StartS", new File("hello"),
				"some-analysis", null);
	}

	/**
	 * Test method for
	 * {@link org.spoofax.sunshine.model.language.Language#AdHocJarBasedLanguage(java.lang.String, java.lang.String[], java.lang.String, java.io.File, java.lang.String, java.io.File)}
	 * .
	 */
	@Test(expected = AssertionError.class)
	public void testAdHocJarBasedLanguage10() {
		new Language("foolang", new String[] { ".txt", ".foo" }, "StartS", new File("hello"),
				"some-analysis", new File("jar"));
	}

	/**
	 * Test method for
	 * {@link org.spoofax.sunshine.model.language.Language#AdHocJarBasedLanguage(java.lang.String, java.lang.String[], java.lang.String, java.io.File, java.lang.String, java.io.File)}
	 * .
	 */
	@Test(expected = AssertionError.class)
	public void testAdHocJarBasedLanguage11() {
		new Language("foolang", new String[] { ".txt", ".foo" }, "StartS", new File("hello"), "",
				new File("jar.jar"));
	}
}
