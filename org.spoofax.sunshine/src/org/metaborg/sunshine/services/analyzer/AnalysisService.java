package org.metaborg.sunshine.services.analyzer;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metaborg.sunshine.CompilerException;
import org.metaborg.sunshine.environment.LaunchConfiguration;
import org.metaborg.sunshine.environment.ServiceRegistry;
import org.metaborg.sunshine.model.messages.IMessage;
import org.metaborg.sunshine.model.messages.MessageHelper;
import org.metaborg.sunshine.model.messages.MessageSeverity;
import org.metaborg.sunshine.services.RuntimeService;
import org.metaborg.sunshine.services.language.ALanguage;
import org.metaborg.sunshine.services.language.LanguageService;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.HybridInterpreter;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class AnalysisService {
	private static final Logger logger = LogManager
			.getLogger(AnalysisService.class.getName());

	private final static String ANALYSIS_CRASHED_MSG = "Analysis failed";

	/**
	 * Run the analysis on the given files. The analysis is started on all files
	 * on a per-language
	 * basis.
	 * 
	 * @see #analyze(File)
	 * @param files
	 * @throws CompilerException
	 */
	public Collection<AnalysisResult> analyze(Collection<File> files)
			throws CompilerException {
		logger.debug("Analyzing {} files", files.size());
		Map<ALanguage, Collection<File>> lang2files = new HashMap<ALanguage, Collection<File>>();
		LanguageService languageService = ServiceRegistry.INSTANCE()
				.getService(LanguageService.class);
		for (File file : files) {
			final ALanguage lang = languageService.getLanguageByExten(file);
			if (lang2files.get(lang) == null) {
				lang2files.put(lang, new LinkedList<File>());
			}
			lang2files.get(lang).add(file);
		}
		logger.trace("Files grouped in {} languages", lang2files.size());
		final Collection<AnalysisResult> results = new HashSet<AnalysisResult>();
		for (ALanguage lang : lang2files.keySet()) {
			results.addAll(analyze(lang, lang2files.get(lang)));
		}
		return results;
	}

	private Collection<AnalysisResult> analyze(ALanguage lang,
			Collection<File> files) throws CompilerException {
		logger.debug("Analyzing {} files of the {} language", files.size(),
				lang.getName());
		ServiceRegistry serviceRegistry = ServiceRegistry.INSTANCE();
		LaunchConfiguration launch = serviceRegistry
				.getService(LaunchConfiguration.class);
		ITermFactory termFactory = launch.termFactory;
		HybridInterpreter runtime = serviceRegistry.getService(
				RuntimeService.class).getRuntime(lang);
		assert runtime != null;

		final Collection<IStrategoString> fileNames = new LinkedList<IStrategoString>();
		for (File file : files) {
			fileNames.add(termFactory.makeString(launch.projectDir.toURI()
					.relativize(file.toURI()).toString()));
		}
		logger.trace("Converted file names to Stratego strings");
		final IStrategoList inputTerm = termFactory.makeList(fileNames);
		runtime.setCurrent(inputTerm);
		logger.trace("Input term set to {}", inputTerm);

		final Collection<AnalysisResult> results = new HashSet<AnalysisResult>();
		try {
			final String function = lang.getAnalysisFunction();
			logger.debug("Invoking analysis strategy {}", function);
			boolean success = runtime.invoke(function);
			logger.debug("Analysis completed with success: {}", success);
			if (!success) {
				throw new CompilerException(ANALYSIS_CRASHED_MSG);
			} else {
				if (!(runtime.current() instanceof IStrategoTuple)) {
					logger.fatal("Unexpected results from analysis {}",
							runtime.current());
					throw new CompilerException(
							"Unexpected results from analysis: "
									+ runtime.current());
				}
				final IStrategoTuple resultTup = (IStrategoTuple) runtime
						.current();
				logger.trace("Analysis resulted in a {} tuple",
						resultTup.getSubtermCount());
				final IStrategoList resultList = (IStrategoList) resultTup
						.getSubterm(0);
				final int numItems = resultList.getSubtermCount();
				logger.trace(
						"Analysis contains {} results. Marshalling to analysis results.",
						numItems);
				for (int idx = 0; idx < numItems; idx++) {
					results.add(makeAnalysisResult(resultList.getSubterm(idx)));
				}
			}
		} catch (InterpreterException interpex) {
			throw new CompilerException(ANALYSIS_CRASHED_MSG, interpex);
		}
		logger.debug("Analysis done");
		return results;
	}

	private AnalysisResult makeAnalysisResult(IStrategoTerm res) {
		assert res != null;
		assert res.getSubtermCount() == 7;
		File file = new File(
				((IStrategoString) res.getSubterm(0)).stringValue());
		Collection<IMessage> messages = new HashSet<IMessage>();
		messages.addAll(MessageHelper.makeMessages(file, MessageSeverity.ERROR,
				(IStrategoList) res.getSubterm(4)));
		messages.addAll(MessageHelper.makeMessages(file,
				MessageSeverity.WARNING, (IStrategoList) res.getSubterm(5)));
		messages.addAll(MessageHelper.makeMessages(file, MessageSeverity.NOTE,
				(IStrategoList) res.getSubterm(6)));
		IStrategoTerm ast = res.getSubterm(2);
		IStrategoTerm previousAst = res.getSubterm(1);

		return new AnalysisResult(new AnalysisResult(null, file,
				Arrays.asList(new IMessage[] {}), previousAst), file, messages,
				ast);
	}

}
