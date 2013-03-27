package org.spoofax.sunshine.framework.services;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.spoofax.interpreter.core.InterpreterErrorExit;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.core.InterpreterExit;
import org.spoofax.interpreter.core.UndefinedStrategyException;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.sunshine.CompilerException;
import org.spoofax.sunshine.Environment;
import org.spoofax.sunshine.framework.language.ALanguage;
import org.spoofax.sunshine.framework.messages.Message;
import org.spoofax.sunshine.framework.messages.MessageHelper;
import org.spoofax.sunshine.framework.messages.MessageType;
import org.spoofax.sunshine.framework.messages.ResultApplAnalysisResult;
import org.strategoxt.HybridInterpreter;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class AnalysisService {

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
	 * Run analysis on the given file. The {@link ALanguage} is determined by the file extension.
	 * The analysis function is subsequently obtained from the determined {@link ALanguage}. Wraps
	 * {@link #analyze(Collection)}.
	 * 
	 * @param file
	 * @throws CompilerException
	 */
	public void analyze(File file) throws CompilerException {
		analyze(Arrays.asList(file));
	}

	/**
	 * Run the analysis on the given files. The analysis is started on all files on a per-language
	 * basis.
	 * 
	 * @see #analyze(File)
	 * @param files
	 * @throws CompilerException
	 */
	public void analyze(Collection<File> files) throws CompilerException {
		final Map<ALanguage, Collection<File>> lang2files = new HashMap<ALanguage, Collection<File>>();
		for (File file : files) {
			final ALanguage lang = LanguageService.INSTANCE().getLanguageByExten(file);
			if (lang2files.get(lang) == null) {
				lang2files.put(lang, new LinkedList<File>());
			}
			lang2files.get(lang).add(file);
		}

		for (ALanguage lang : lang2files.keySet()) {
			analyze(lang, lang2files.get(lang));
		}
	}

	private void analyze(ALanguage lang, Collection<File> files) throws CompilerException {
		final ITermFactory termFactory = Environment.INSTANCE().termFactory;
		final HybridInterpreter runtime = RuntimeService.INSTANCE().getRuntime(lang);
		assert runtime != null;

		final Collection<IStrategoString> fileNames = new LinkedList<IStrategoString>();
		for (File file : files) {
			MessageService.INSTANCE().clearMessages(file.getPath(), MessageType.ANALYSIS_MESSAGE);
			fileNames.add(termFactory.makeString(file.getPath()));
		}

		final IStrategoList inputTerm = termFactory.makeList(fileNames);
		runtime.setCurrent(inputTerm);
		try {
			boolean success = runtime.invoke(lang.getAnalysisFunction());
			if (!success) {
				reportAnalysisException(files, new RuntimeException("Analysis function failed w/o exception"));
			} else {
				final IStrategoTuple resultTup = (IStrategoTuple) runtime.current();
				final IStrategoList resultList = (IStrategoList) resultTup.getSubterm(1);
				for (int idx = 0; idx < resultList.getSubtermCount(); idx++) {
					AnalysisResultsService.INSTANCE().addResult(
							new ResultApplAnalysisResult((IStrategoAppl) resultList.getSubterm(idx)));
				}
			}
		} catch (InterpreterErrorExit e) {
			reportAnalysisException(files, e);
		} catch (InterpreterExit e) {
			reportAnalysisException(files, e);
		} catch (UndefinedStrategyException e) {
			reportAnalysisException(files, e);
		} catch (InterpreterException e) {
			reportAnalysisException(files, e);
		}
	}

	private static void reportAnalysisException(Collection<File> files, Throwable t) throws CompilerException {
		assert !files.isEmpty();
		final File oneFile = files.iterator().next();
		final Message msg = MessageHelper.newAnalysisErrorAtTop(oneFile.getPath(), "Analysis crashed");
		msg.exception = t;
		MessageService.INSTANCE().addMessage(msg);
		t.printStackTrace();
		throw new CompilerException("Analysis failed", t);
	}

	// public void storeResults(File file, IStrategoTerm tup) {
	// assert isTermTuple(tup);
	// assert tup.getSubtermCount() == 4;
	//
	// final IAnalysisResult result = new TupleBasedAnalysisResult(file, tup);
	// results.put(file, result);
	// MessageService.INSTANCE().addMessage(result.getMessages());
	// }

}
