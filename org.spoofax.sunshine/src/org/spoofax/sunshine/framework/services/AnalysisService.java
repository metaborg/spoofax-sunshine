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
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.sunshine.Environment;
import org.spoofax.sunshine.framework.language.ALanguage;
import org.strategoxt.HybridInterpreter;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class AnalysisService {

	private static AnalysisService INSTANCE;

	private Map<File, IStrategoTerm> asts = new HashMap<File, IStrategoTerm>();

	private AnalysisService() {
	}

	public static final AnalysisService INSTANCE() {
		if (INSTANCE == null) {
			INSTANCE = new AnalysisService();
		}
		return INSTANCE;
	}

	public IStrategoTerm getAst(File file) {
		IStrategoTerm ast = asts.get(file);
		if (ast == null) {
			analyze(file);
			ast = asts.get(file);
		}
		return ast;
	}

	public void analyze(File file) {
		analyze(Arrays.asList(file));
	}

	public void analyze(Collection<File> files) {
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

	public void analyze(ALanguage lang, Collection<File> files) {
		final ITermFactory termFactory = Environment.INSTANCE().termFactory;
		final HybridInterpreter runtime = RuntimeService.INSTANCE().getRuntime(lang);
		assert runtime != null;

		final Collection<IStrategoString> fileNames = new LinkedList<IStrategoString>();
		for (File file : files) {
			fileNames.add(termFactory.makeString(file.getPath()));
		}

		final IStrategoList inputTerm = termFactory.makeList(fileNames);
		runtime.setCurrent(inputTerm);
		try {
			boolean success = runtime.invoke(lang.getAnalysisFunction());
			if (!success) {
				reportAnalysisException(files, new RuntimeException("Analysis function failed"));
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

	private void reportAnalysisException(Collection<File> files, Throwable t) {
		System.out.println("Analysis failed: ");
		t.printStackTrace();
	}

}
