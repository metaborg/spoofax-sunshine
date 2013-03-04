package org.spoofax.sunshine.parser.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.ITokenizer;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.jsglr.client.imploder.NullTokenizer;
import org.spoofax.jsglr.io.FileTools;
import org.spoofax.sunshine.framework.language.ILanguage;
import org.spoofax.sunshine.framework.services.LanguageService;
import org.spoofax.sunshine.parser.framework.IParseController;
import org.spoofax.sunshine.parser.framework.IParser;
import org.spoofax.sunshine.parser.framework.IParserConfig;
import org.spoofax.sunshine.parser.framework.ParserException;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class JSGLRParseController implements IParseController {
	public final static int PARSE_TIMEOUT = 5000;

	private final File file;
	private IParser parser;
	private final JSGLRParseErrorHandler errorHandler;
	private final IParserConfig parserConfig;
	
	private IStrategoTerm currentAst;
	private ITokenizer currentTokenizer;

	public JSGLRParseController(File f) {
		this.file = f;
		final ILanguage lang = LanguageService.INSTANCE().getLanguageByExten(f);
		parserConfig = new JSGLRConfig(lang.getStartSymbol(), lang.getParseTableProvider(),
				PARSE_TIMEOUT);
		errorHandler = new JSGLRParseErrorHandler(this);
	}

	@Override
	public File getFile() {
		return this.file;
	}

	@Override
	public IStrategoTerm getCurrentAst() {
		if (currentAst == null) {
			parse();
		}
		return this.currentAst;
	}

	@Override
	public IStrategoTerm parse() {
		String contents;
		try {
			if(parser == null){
				parser = new JSGLRI(parserConfig);
			}
			contents = FileTools.loadFileAsString(new BufferedReader(new InputStreamReader(new FileInputStream(
					this.file))));
			final String filename = this.file.getAbsolutePath();

			currentTokenizer = new NullTokenizer(contents, filename);
			currentAst = parser.parse(contents, filename);
			currentTokenizer = ImploderAttachment.getTokenizer(currentAst);
			
		} catch (FileNotFoundException e) {
			errorHandler.reportException(currentTokenizer, e);
		} catch (IOException e) {
			errorHandler.reportException(currentTokenizer, e);
		} catch (ParserException e) {
			errorHandler.reportException(currentTokenizer, e);
		}
		return currentAst;
	}

}
