package org.spoofax.sunshine.framework.services;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.sunshine.framework.language.LanguageException;
import org.spoofax.sunshine.parser.framework.ParserException;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class AnalysisService {
	private static AnalysisService INSTANCE;

	private Map<File, IStrategoTerm> pAstCache = new HashMap<File, IStrategoTerm>();
	private Map<File, IStrategoTerm> aAstCache = new HashMap<File, IStrategoTerm>();
	private Map<File, Long> cacheTime = new HashMap<File, Long>();

	private AnalysisService() {
	}

	public static final AnalysisService INSTANCE() {
		if (INSTANCE == null) {
			INSTANCE = new AnalysisService();
		}
		return INSTANCE;
	}

	public IStrategoTerm getAnalyzedAst(File f) throws AnalysisException {
		return getAnalyzedAst(f, false);
	}

	public IStrategoTerm getAnalyzedAst(File f, boolean reanalyze) throws AnalysisException {
		// TODO refactor this to returned the cached AST if parsing fails
		boolean doAnalyze = reanalyze;
		boolean doParse = false;

		final Long astTime = cacheTime.get(f);
		if (astTime == null || astTime < f.lastModified()) {
			doAnalyze = true;
			doParse = true;
		}

		IStrategoTerm result = null;
		if (doParse) {
			try {
				result = ParseService.INSTANCE().parse(f);
			} catch (Exception e) {
				throw new AnalysisException("Analysis failed: ", e);
			}
			if(result == null){
				result = pAstCache.get(f);
			}else{
				pAstCache.put(f, result);
			}
		}else{
			result = pAstCache.get(f);
		}

		// TODO hook in analysis

		return result;
	}

}
