/**
 * 
 */
package org.spoofax.sunshine.services.analyzer.legacy;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.sunshine.CompilerException;
import org.spoofax.sunshine.Environment;
import org.spoofax.sunshine.model.language.ALanguage;
import org.spoofax.sunshine.parser.model.IStrategoParseOrAnalyzeResult;
import org.spoofax.sunshine.pipeline.connectors.ALinkOneToOne;
import org.spoofax.sunshine.pipeline.diff.Diff;
import org.spoofax.sunshine.services.LanguageService;
import org.spoofax.sunshine.services.RuntimeService;
import org.spoofax.sunshine.services.parser.SourceAttachment;
import org.strategoxt.HybridInterpreter;

/**
 * @author vladvergu
 * 
 */
public class LegacyAnalyzerLink extends ALinkOneToOne<IStrategoTerm, IStrategoParseOrAnalyzeResult> {

	private final static String ANALYSIS_CRASHED_MSG = "Analysis failed";

	private static final Logger logger = LogManager.getLogger(LegacyAnalyzerLink.class.getName());

	@Override
	public Diff<IStrategoParseOrAnalyzeResult> sinkWork(Diff<IStrategoTerm> input) {
		IStrategoTerm ast = input.getPayload();
		return new Diff<IStrategoParseOrAnalyzeResult>(analyze(SourceAttachment.getResource(ast),
				ast), input.getDiffKind());
	}

	private IStrategoParseOrAnalyzeResult analyze(File file, IStrategoTerm ast) {
		logger.debug("Analyzing AST of file {}", file);
		ALanguage lang = LanguageService.INSTANCE().getLanguageByExten(file);

		ITermFactory termFactory = Environment.INSTANCE().termFactory;
		HybridInterpreter runtime = RuntimeService.INSTANCE().getRuntime(lang);

		IStrategoString fileTerm = termFactory.makeString(Environment.INSTANCE().projectDir.toURI()
				.relativize(file.toURI()).toString());
		IStrategoString projectTerm = termFactory.makeString(Environment.INSTANCE().projectDir
				.getAbsolutePath());

		IStrategoTuple inputTerm = termFactory.makeTuple(ast, fileTerm, projectTerm);
		runtime.setCurrent(inputTerm);
		String function = lang.getAnalysisFunction();
		boolean success;
		try {
			success = runtime.invoke(function);
			if (!success) {
				throw new CompilerException(ANALYSIS_CRASHED_MSG);
			} else {
				logger.warn("Ignoring further files to analyze. Not implemented");
				IStrategoTuple resultTuple = (IStrategoTuple) runtime.current();
				logger.trace("Analysis resulted in a {} tuple", resultTuple.getSubtermCount());
				return new LegacyAnalysisResult(file, ast, resultTuple);
			}
		} catch (InterpreterException e) {
			throw new CompilerException(ANALYSIS_CRASHED_MSG, e);
		}
	}
}
