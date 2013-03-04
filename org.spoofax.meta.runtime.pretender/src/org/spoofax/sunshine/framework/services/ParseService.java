/**
 * 
 */
package org.spoofax.sunshine.framework.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.sunshine.framework.language.ILanguage;
import org.spoofax.sunshine.framework.language.LanguageException;
import org.spoofax.sunshine.framework.language.MissingLanguage;
import org.spoofax.sunshine.parser.framework.FileBasedParseTableProvider;
import org.spoofax.sunshine.parser.framework.IParseTableProvider;
import org.spoofax.sunshine.parser.framework.IParser;
import org.spoofax.sunshine.parser.framework.ParserException;
import org.spoofax.sunshine.parser.jsglr.JSGLRConfig;
import org.spoofax.sunshine.parser.jsglr.JSGLRI;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 *
 */
public class ParseService {
	public final static int PARSE_TIMEOUT = 5000;
	
	private static ParseService INSTANCE;
	
	private final Map<ILanguage, IParser> parsers = new HashMap<ILanguage, IParser>();
	
	private ParseService(){
	}
	
	public static final ParseService INSTANCE() {
		if(INSTANCE == null){
			INSTANCE = new ParseService();
		}
		return INSTANCE;
	}
	
	private IParser getParser(ILanguage lang) throws ParserException {
		IParser parser = parsers.get(lang);
		if(parser == null){
			final IParseTableProvider tblProv = new FileBasedParseTableProvider(lang.getParseTable());
			final JSGLRConfig pConf = new JSGLRConfig(lang.getStartSymbol(), tblProv, PARSE_TIMEOUT);
			parser = new JSGLRI(pConf);
		}
		return parser;
	}
	
	public IStrategoTerm parse(File input, ILanguage lang) throws ParserException {
		final IParser parser = getParser(lang);
		try {
			return parser.parse(new FileInputStream(input), input.getName());
		} catch (FileNotFoundException e) {
			throw new ParserException("File not found: " + input.getAbsolutePath());
		}
	}
	
	public IStrategoTerm parse(File input) throws LanguageException, ParserException {
		final ILanguage lang = LanguageService.INSTANCE().getLanguageByExten(input);
		if(lang == null){
			throw new MissingLanguage("No language for file " + input.getAbsolutePath());
		}
		return parse(input, lang);
	}
	
}
