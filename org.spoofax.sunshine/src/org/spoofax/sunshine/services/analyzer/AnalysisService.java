package org.spoofax.sunshine.services.analyzer;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.sunshine.CompilerException;
import org.spoofax.sunshine.Environment;
import org.spoofax.sunshine.model.language.ALanguage;
import org.spoofax.sunshine.parser.model.IStrategoParseOrAnalyzeResult;
import org.spoofax.sunshine.services.LanguageService;
import org.spoofax.sunshine.services.RuntimeService;
import org.strategoxt.HybridInterpreter;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class AnalysisService {
	private static final Logger logger = LogManager.getLogger(AnalysisService.class.getName());

	private final static String ANALYSIS_CRASHED_MSG = "Analysis failed";

	private static AnalysisService INSTANCE;

	protected AnalysisService() {
	}

	public static final AnalysisService INSTANCE() {
		if (INSTANCE == null) {
			INSTANCE = new AnalysisService();
		}
		return INSTANCE;
	}

	/**
	 * Run the analysis on the given files. The analysis is started on all files on a per-language
	 * basis.
	 * 
	 * @see #analyze(File)
	 * @param files
	 * @throws CompilerException
	 */
	public Collection<IStrategoParseOrAnalyzeResult> analyze(Collection<File> files)
			throws CompilerException {
		logger.debug("Analyzing {} files", files.size());
		final Map<ALanguage, Collection<File>> lang2files = new HashMap<ALanguage, Collection<File>>();
		for (File file : files) {
			final ALanguage lang = LanguageService.INSTANCE().getLanguageByExten(file);
			if (lang2files.get(lang) == null) {
				lang2files.put(lang, new LinkedList<File>());
			}
			lang2files.get(lang).add(file);
		}
		logger.trace("Files grouped in {} languages", lang2files.size());
		final Collection<IStrategoParseOrAnalyzeResult> results = new HashSet<IStrategoParseOrAnalyzeResult>();
		for (ALanguage lang : lang2files.keySet()) {
			results.addAll(analyze(lang, lang2files.get(lang)));
		}
		return results;
	}

	private Collection<IStrategoParseOrAnalyzeResult> analyze(ALanguage lang, Collection<File> files)
			throws CompilerException {
		logger.debug("Analyzing {} files of the {} language", files.size(), lang.getName());
		final ITermFactory termFactory = Environment.INSTANCE().termFactory;
		final HybridInterpreter runtime = RuntimeService.INSTANCE().getRuntime(lang);
		assert runtime != null;

		final Collection<IStrategoString> fileNames = new LinkedList<IStrategoString>();
		for (File file : files) {
			fileNames.add(termFactory.makeString(Environment.INSTANCE().projectDir.toURI()
					.relativize(file.toURI()).toString()));
			// fileNames.add(termFactory.makeString(file.getPath()));
		}
		logger.trace("Converted file names to Stratego strings");
		final IStrategoList inputTerm = termFactory.makeList(fileNames);
		runtime.setCurrent(inputTerm);
		logger.trace("Input term set to {}", inputTerm);

		final Collection<IStrategoParseOrAnalyzeResult> results = new HashSet<IStrategoParseOrAnalyzeResult>();
		try {
			final String function = lang.getAnalysisFunction();
			logger.debug("Invoking analysis strategy {}", function);
			boolean success = runtime.invoke(function);
			logger.debug("Analysis completed with success: {}", success);
			if (!success) {
				throw new CompilerException(ANALYSIS_CRASHED_MSG);
			} else {
				final IStrategoTuple resultTup = (IStrategoTuple) runtime.current();
				logger.trace("Analysis resulted in a {} tuple", resultTup.getSubtermCount());
				final IStrategoList resultList = (IStrategoList) resultTup.getSubterm(1);
				final int numItems = resultList.getSubtermCount();
				logger.trace("Analysis contains {} results. Marshalling to analysis results.",
						numItems);
				for (int idx = 0; idx < numItems; idx++) {
					results.add(new ResultApplAnalysisResult((IStrategoAppl) resultList
							.getSubterm(idx)));
				}
			}
		} catch (InterpreterException interpex) {
			throw new CompilerException(ANALYSIS_CRASHED_MSG, interpex);
		}
		logger.debug("Analysis done");
		return results;
	}
}
