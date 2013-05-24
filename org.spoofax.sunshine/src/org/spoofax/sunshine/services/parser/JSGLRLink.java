/**
 * 
 */
package org.spoofax.sunshine.services.parser;

import java.io.File;
import java.util.Map;
import java.util.WeakHashMap;

import org.spoofax.sunshine.CompilerException;
import org.spoofax.sunshine.model.language.ALanguage;
import org.spoofax.sunshine.parser.model.IStrategoParseOrAnalyzeResult;
import org.spoofax.sunshine.parser.model.ParserConfig;
import org.spoofax.sunshine.pipeline.connectors.ALinkOneToOne;
import org.spoofax.sunshine.pipeline.diff.Diff;
import org.spoofax.sunshine.services.LanguageService;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class JSGLRLink extends ALinkOneToOne<File, IStrategoParseOrAnalyzeResult> {
	public final static int PARSE_TIMEOUT = 5000;

	private final Map<File, JSGLRI> parsers = new WeakHashMap<File, JSGLRI>();

	@Override
	public Diff<IStrategoParseOrAnalyzeResult> sinkWork(Diff<File> input) {
		JSGLRI parser = parsers.get(input.getPayload());
		if (parser == null) {
			parser = constructParser(input.getPayload());
			parsers.put(input.getPayload(), parser);
		}
		assert parser != null;

		JSGLRParseResult parseResult = parser.parse();
		return new Diff<IStrategoParseOrAnalyzeResult>(parseResult, input.getDiffKind());
	}

	private JSGLRI constructParser(File file) {
		ALanguage lang = LanguageService.INSTANCE().getLanguageByExten(file);
		if (lang == null) {
			throw new CompilerException("No language registered for file " + file.getPath());
		}
		ParserConfig config = new ParserConfig(lang.getStartSymbol(), lang.getParseTableProvider(),
				PARSE_TIMEOUT);

		return new JSGLRI(config, file);
	}

}
