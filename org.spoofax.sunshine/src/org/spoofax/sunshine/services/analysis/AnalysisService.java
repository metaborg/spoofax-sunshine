package org.spoofax.sunshine.services.analysis;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.sunshine.CompilerException;
import org.spoofax.sunshine.Environment;
import org.spoofax.sunshine.model.language.ALanguage;
import org.spoofax.sunshine.services.LanguageService;
import org.spoofax.sunshine.services.RuntimeService;
import org.strategoxt.HybridInterpreter;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class AnalysisService {
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
     * Run the analysis on the given files. The analysis is started on all files
     * on a per-language basis.
     * 
     * @see #analyze(File)
     * @param files
     * @throws CompilerException
     */
    public Collection<IAnalysisResult> analyze(Collection<File> files)
	    throws CompilerException {
	final Map<ALanguage, Collection<File>> lang2files = new HashMap<ALanguage, Collection<File>>();
	for (File file : files) {
	    final ALanguage lang = LanguageService.INSTANCE()
		    .getLanguageByExten(file);
	    if (lang2files.get(lang) == null) {
		lang2files.put(lang, new LinkedList<File>());
	    }
	    lang2files.get(lang).add(file);
	}
	final Collection<IAnalysisResult> results = new HashSet<IAnalysisResult>();
	for (ALanguage lang : lang2files.keySet()) {
	    results.addAll(analyze(lang, lang2files.get(lang)));
	}
	return results;
    }

    private Collection<IAnalysisResult> analyze(ALanguage lang,
	    Collection<File> files) throws CompilerException {
	final ITermFactory termFactory = Environment.INSTANCE().termFactory;
	final HybridInterpreter runtime = RuntimeService.INSTANCE().getRuntime(
		lang);
	assert runtime != null;

	final Collection<IStrategoString> fileNames = new LinkedList<IStrategoString>();
	for (File file : files) {
	    fileNames.add(termFactory.makeString(file.getPath()));
	}

	final IStrategoList inputTerm = termFactory.makeList(fileNames);
	runtime.setCurrent(inputTerm);

	final Collection<IAnalysisResult> results = new HashSet<IAnalysisResult>();
	try {
	    boolean success = runtime.invoke(lang.getAnalysisFunction());
	    if (!success) {
		throw new CompilerException(ANALYSIS_CRASHED_MSG);
	    } else {
		final IStrategoTuple resultTup = (IStrategoTuple) runtime
			.current();
		final IStrategoList resultList = (IStrategoList) resultTup
			.getSubterm(1);
		for (int idx = 0; idx < resultList.getSubtermCount(); idx++) {
		    results.add(new ResultApplAnalysisResult(
			    (IStrategoAppl) resultList.getSubterm(idx)));
		}
	    }
	} catch (InterpreterException interpex) {
	    throw new CompilerException(ANALYSIS_CRASHED_MSG, interpex);
	}

	return results;
    }
}
