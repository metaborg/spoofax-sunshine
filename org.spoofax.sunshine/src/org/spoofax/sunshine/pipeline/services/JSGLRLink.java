/**
 * 
 */
package org.spoofax.sunshine.pipeline.services;

import java.io.File;
import java.util.Map;
import java.util.WeakHashMap;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.sunshine.CompilerException;
import org.spoofax.sunshine.model.language.ALanguage;
import org.spoofax.sunshine.parser.impl.JSGLRI;
import org.spoofax.sunshine.parser.impl.JSGLRParseResult;
import org.spoofax.sunshine.parser.model.IParseResult;
import org.spoofax.sunshine.parser.model.ParserConfig;
import org.spoofax.sunshine.pipeline.connectors.ALinkOneToOne;
import org.spoofax.sunshine.pipeline.diff.Diff;
import org.spoofax.sunshine.services.LanguageService;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class JSGLRLink extends ALinkOneToOne<File, IParseResult<IStrategoTerm>> {
    public final static int PARSE_TIMEOUT = 5000;

    private final Map<File, JSGLRI> parsers = new WeakHashMap<File, JSGLRI>();

    @Override
    public Diff<IParseResult<IStrategoTerm>> sinkWork(Diff<File> input) {
	JSGLRI parser = parsers.get(input.getPayload());
	if (parser == null) {
	    parser = constructParser(input.getPayload());
	    parsers.put(input.getPayload(), parser);
	}
	assert parser != null;

	JSGLRParseResult parseResult = parser.parse();
	return new Diff<IParseResult<IStrategoTerm>>(parseResult,
		input.getDiffKind());
    }

    private JSGLRI constructParser(File file) {
	ALanguage lang = LanguageService.INSTANCE().getLanguageByExten(file);
	if (lang == null) {
	    throw new CompilerException("No language registered for file "
		    + file.getPath());
	}
	ParserConfig config = new ParserConfig(lang.getStartSymbol(),
		lang.getParseTableProvider(), PARSE_TIMEOUT);

	return new JSGLRI(config, file);
    }

}
