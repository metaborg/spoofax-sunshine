package org.spoofax.sunshine.parser.framework;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.io.FileTools;
import org.spoofax.jsglr.shared.BadTokenException;
import org.spoofax.jsglr.shared.SGLRException;
import org.spoofax.jsglr.shared.TokenExpectedException;
import org.spoofax.sunshine.messages.IMessageProducer;
import org.strategoxt.imp.runtime.parser.CustomDisambiguator;
import org.strategoxt.imp.runtime.stratego.SourceAttachment;

/**
 * An abstract imploding SGLR parser class.
 * 
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 */
public abstract class ASGLRI implements IParser, IMessageProducer {

	protected IParserConfig config;

//	private CustomDisambiguator disambiguator;

	// Simple accessors

	private boolean implodeEnabled = true;

	public ASGLRI() {
	}

	@Override
	public IParserConfig getConfiguration() {
		return config;
	}

	@Override
	public void setConfiguration(IParserConfig config) {
		this.config = config;
	}

	public int getEOFTokenKind() {
		return IToken.TK_EOF;
	}

	public boolean isImplodeEnabled() {
		return implodeEnabled;
	}

	/**
	 * Parse an input, returning the AST and initializing the parse stream. Also initializes a new
	 * tokenizer for the given input.
	 * 
	 * @note This redirects to the preferred {@link #parse(String, String)} method.
	 * 
	 * @return The abstract syntax tree.
	 * @throws InterruptedException
	 */
	public final IStrategoTerm parse(InputStream input, String filename) throws ParserException {
		String inputString;
		try {
			inputString = FileTools.loadFileAsString(new BufferedReader(new InputStreamReader(input)));
			return parse(inputString, filename);
		} catch (IOException e) {
			throw new ParserException("Parse failed", e);
		}
	}

	/**
	 * Parse an input, returning the AST and initializing the parse stream.
	 * 
	 * @return The abstract syntax tree.
	 */
	public IStrategoTerm parse(String input, String filename) throws ParserException {

		final IStrategoTerm ambAst = doParse(input, filename);
		if (ambAst == null) {
			return null;
		}
		SourceAttachment.putSource(ambAst, new File(filename), config);
//		final IStrategoTerm ast = disambiguator == null ? parsedResult : disambiguator
//				.disambiguate(ambAst);
//
//		return ast;
		return ambAst;
	}

//	public void setCustomDisambiguator(CustomDisambiguator disambiguator) {
//		this.disambiguator = disambiguator;
//	}

	public void setImplodeEnabled(boolean implodeEnabled) {
		this.implodeEnabled = implodeEnabled;
	}

	protected abstract IStrategoTerm doParse(String input, String filename) throws ParserException;
}