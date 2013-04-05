/**
 * 
 */
package org.spoofax.sunshine.parser.framework;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.ParseTable;
import org.spoofax.sunshine.Environment;
import org.spoofax.sunshine.parser.model.IParseTableProvider;
import org.spoofax.sunshine.parser.model.IParserConfig;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class AParserTest {

	AParser parser;
	IParserConfig config = new IParserConfig() {

		@Override
		public int getTimeout() {
			return 42;
		}

		@Override
		public String getStartSymbol() {
			return "start";
		}

		@Override
		public IParseTableProvider getParseTableProvider() {
			return new IParseTableProvider() {

				@Override
				public ParseTable getParseTable() throws ParserException {
					return null;
				}
			};
		}
	};

	IStrategoTerm ast = Environment.INSTANCE().termFactory.makeList();

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {

		parser = new AParser(config) {

			@Override
			protected IStrategoTerm doParse(String input, String filename) throws ParserException {
				return ast;
			}
		};
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		parser = null;
	}

	/**
	 * Test method for
	 * {@link org.spoofax.sunshine.parser.framework.AParser#AParser(org.spoofax.sunshine.parser.model.IParserConfig)}
	 * .
	 */
	@Test(expected = AssertionError.class)
	public void testAParser() {
		new AParser(null) {

			@Override
			protected IStrategoTerm doParse(String input, String filename) throws ParserException {
				return null;
			}

		};
	}

	/**
	 * Test method for {@link org.spoofax.sunshine.parser.framework.AParser#getConfig()}.
	 */
	@Test
	public void testGetConfig() {
		assertEquals(config, parser.getConfig());
	}

	/**
	 * Test method for
	 * {@link org.spoofax.sunshine.parser.framework.AParser#parse(java.lang.String, java.lang.String)}
	 * .
	 * 
	 * @throws ParserException
	 */
	@Test
	public void testParse1() throws ParserException {
		IStrategoTerm result = parser.parse("foobar bar bar", "foobar/barbar");
		assertEquals(ast, result);
	}

	/**
	 * Test method for
	 * {@link org.spoofax.sunshine.parser.framework.AParser#parse(java.lang.String, java.lang.String)}
	 * .
	 */
	@Test(expected = AssertionError.class)
	public void testParse2() throws ParserException {
		parser.parse(null, "foobar/barbar");
	}

	/**
	 * Test method for
	 * {@link org.spoofax.sunshine.parser.framework.AParser#parse(java.lang.String, java.lang.String)}
	 * .
	 */
	@Test(expected = AssertionError.class)
	public void testParse3() throws ParserException {
		parser.parse("foo", null);
	}

}
