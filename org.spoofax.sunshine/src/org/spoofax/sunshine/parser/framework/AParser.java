package org.spoofax.sunshine.parser.framework;

import java.io.File;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.sunshine.parser.impl.SourceAttachment;

/**
 * An abstract imploding SGLR parser class.
 * 
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 */
public abstract class AParser implements IParser {

	protected IParserConfig config;

	// private CustomDisambiguator disambiguator;

	// Simple accessors

	public AParser(IParserConfig config) {
		assert config != null;
		this.config = config;
	}

	@Override
	public IParserConfig getConfig() {
		return config;
	}

	/**
	 * Parse an input, returning the AST and initializing the parse stream.
	 * 
	 * @return The abstract syntax tree.
	 */
	public IStrategoTerm parse(String input, String filename) throws ParserException {
		assert input != null;
		assert filename != null;
		final IStrategoTerm ambAst = doParse(input, filename);
		if (ambAst == null) {
			return null;
		}
		SourceAttachment.putSource(ambAst, new File(filename), config);
		// final IStrategoTerm ast = disambiguator == null ? parsedResult : disambiguator
		// .disambiguate(ambAst);
		//
		// return ast;
		return ambAst;
	}

	// public void setCustomDisambiguator(CustomDisambiguator disambiguator) {
	// this.disambiguator = disambiguator;
	// }

	protected abstract IStrategoTerm doParse(String input, String filename) throws ParserException;
}