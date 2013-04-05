/**
 * 
 */
package org.spoofax.sunshine.parser.framework;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spoofax.jsglr.client.ParseTable;
import org.spoofax.sunshine.parser.impl.FileBasedParseTableProvider;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class FileBasedParseTableProviderTest {

	File tbl;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		tbl = new File(this.getClass().getClassLoader().getResource("Entity.tbl").toURI());
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		tbl = null;
	}

	/**
	 * Test method for
	 * {@link org.spoofax.sunshine.parser.impl.FileBasedParseTableProvider#getParseTable()}.
	 */
	@Test
	public void testGetParseTable1() {
		FileBasedParseTableProvider tblp = new FileBasedParseTableProvider(tbl);
		try {
			ParseTable tbl = tblp.getParseTable();
			assertNotNull(tbl);
		} catch (ParserException e) {
			assertFalse(true);
		}
	}

	/**
	 * Test method for
	 * {@link org.spoofax.sunshine.parser.impl.FileBasedParseTableProvider#getParseTable()}.
	 */
	@Test
	public void testGetParseTable2() {
		FileBasedParseTableProvider tblp = new FileBasedParseTableProvider(tbl, true);
		try {
			ParseTable tbl1 = tblp.getParseTable();
			ParseTable tbl2 = tblp.getParseTable();
			assertEquals(tbl1, tbl2);
		} catch (ParserException e) {
			assertFalse(true);
		}
	}

	/**
	 * Test method for
	 * {@link org.spoofax.sunshine.parser.impl.FileBasedParseTableProvider#getParseTable()}.
	 */
	@Test
	public void testGetParseTable3() {
		FileBasedParseTableProvider tblp = new FileBasedParseTableProvider(tbl, false);
		try {
			ParseTable tbl1 = tblp.getParseTable();
			ParseTable tbl2 = tblp.getParseTable();
			assertFalse(tbl1.equals(tbl2));
		} catch (ParserException e) {
			assertFalse(true);
		}
	}

	/**
	 * Test method for
	 * {@link org.spoofax.sunshine.parser.impl.FileBasedParseTableProvider#getParseTable()}.
	 * 
	 * @throws ParserException
	 */
	@Test(expected = ParserException.class)
	public void testGetParseTable4() throws ParserException {
		FileBasedParseTableProvider tblp = new FileBasedParseTableProvider(new File("foo"), true);

		tblp.getParseTable();
	}

	/**
	 * Test method for
	 * {@link org.spoofax.sunshine.parser.impl.FileBasedParseTableProvider#getParseTable()}.
	 */
	@Test(expected = AssertionError.class)
	public void testGetParseTable5() {
		new FileBasedParseTableProvider(null, false);
	}

}
