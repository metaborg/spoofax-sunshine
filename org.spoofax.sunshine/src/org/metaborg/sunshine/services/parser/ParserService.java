/**
 * 
 */
package org.metaborg.sunshine.services.parser;

import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.vfs2.FileObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ILanguageService;
import org.metaborg.spoofax.core.resource.IResourceService;
import org.metaborg.spoofax.core.service.syntax.SyntaxFacet;
import org.metaborg.sunshine.CompilerException;
import org.metaborg.sunshine.parser.model.IParserConfig;
import org.metaborg.sunshine.parser.model.ParserConfig;
import org.metaborg.sunshine.services.analyzer.AnalysisFileResult;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class ParserService {

	public final static int PARSE_TIMEOUT = 5000;

	private static final Logger logger = LogManager
			.getLogger(ParserService.class.getName());

	private final Map<ILanguage, IParserConfig> parserConfigs = new WeakHashMap<ILanguage, IParserConfig>();

	private final ILanguageService languageService;
	private final IResourceService resourceService;

	@Inject
	public ParserService(ILanguageService languageService,
			IResourceService resourceService) {
		this.languageService = languageService;
		this.resourceService = resourceService;
	}

	public AnalysisFileResult parseFile(File file) {
		logger.trace("Parsing file {}", file);
		final FileObject fileObject = resourceService.resolve(file);
		ILanguage lang = languageService.getByExt(fileObject.getName()
				.getExtension());
		if (lang == null) {
			throw new CompilerException("No language registered for file "
					+ file);
		}
		IParserConfig parserConfig = getParserConfig(lang);
		assert parserConfig != null;

		JSGLRI parser = new JSGLRI(parserConfig, file);
		return parser.parse();
	}

	public AnalysisFileResult parse(InputStream is, ILanguage lang) {
		logger.trace("Parsing input stream of language {}", lang.name());
		IParserConfig parserConfig = getParserConfig(lang);
		assert parserConfig != null;

		JSGLRI parser = new JSGLRI(parserConfig, is);
		return parser.parse();
	}

	private IParserConfig getParserConfig(ILanguage lang) {
		IParserConfig parserConfig = parserConfigs.get(lang);
		if (parserConfig == null) {
			final SyntaxFacet syntaxFacet = lang.facet(SyntaxFacet.class);
			parserConfig = new ParserConfig(Iterables.get(
					syntaxFacet.startSymbols(), 0), lang.facet(
					SyntaxFacet.class).parseTableProvider(), PARSE_TIMEOUT);
			parserConfigs.put(lang, parserConfig);
		}
		return parserConfig;
	}

}
