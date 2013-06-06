/**
 * 
 */
package org.spoofax.sunshine.services.parser;

import java.io.File;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spoofax.sunshine.CompilerException;
import org.spoofax.sunshine.model.language.ALanguage;
import org.spoofax.sunshine.parser.model.ParserConfig;
import org.spoofax.sunshine.pipeline.connectors.ALinkOneToOne;
import org.spoofax.sunshine.pipeline.diff.Diff;
import org.spoofax.sunshine.services.LanguageService;
import org.spoofax.sunshine.services.analyzer.AnalysisResult;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class JSGLRLink extends ALinkOneToOne<File, AnalysisResult> {

	private static final Logger logger = LogManager.getLogger(JSGLRLink.class.getName());

	public final static int PARSE_TIMEOUT = 5000;

	private final Map<File, JSGLRI> parsers = new WeakHashMap<File, JSGLRI>();

	@Override
	public Diff<AnalysisResult> sinkWork(Diff<File> input) {
		JSGLRI parser = parsers.get(input.getPayload());
		if (parser == null) {
			parser = constructParser(input.getPayload());
			parsers.put(input.getPayload(), parser);
		}
		assert parser != null;

		AnalysisResult parseResult = parser.parse();
		logger.trace("Parsing of file {} produced AST {} and {} messages", input.getPayload(),
				parseResult.ast(), parseResult.messages().size());
		return new Diff<AnalysisResult>(parseResult, input.getDiffKind());
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
