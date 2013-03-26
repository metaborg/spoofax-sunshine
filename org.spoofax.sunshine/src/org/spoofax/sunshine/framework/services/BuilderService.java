/**
 * 
 */
package org.spoofax.sunshine.framework.services;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.sunshine.CompilerException;
import org.spoofax.sunshine.Environment;
import org.spoofax.sunshine.framework.language.ALanguage;
import org.spoofax.sunshine.framework.messages.IAnalysisResult;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class BuilderService {
	private static BuilderService INSTANCE;

	private BuilderService() {
	}

	public static BuilderService INSTANCE() {
		if (INSTANCE == null) {
			INSTANCE = new BuilderService();
		}
		return INSTANCE;
	}

	/**
	 * Call a builder on the file's source or analyzed AST. The builder is expected to have the
	 * following format:
	 * 
	 * <code>
	 * builder:
	 * 	(node, position, ast, path, project-path) -> (filename, result)
	 * 	
	 * </code>
	 * 
	 * NB: The current implementation calls the builder on the following input:
	 * 
	 * <code>
	 * 	(ast, [], ast, path, project-path)
	 * </code>
	 * 
	 * NB: The current implementation assumes that the result is a StrategoString and will not work
	 * with a term.
	 * 
	 * @param file
	 *            The file to call the builder on
	 * @param builderName
	 *            The name of the builder to call
	 * @param onSourceAST
	 *            If <code>true</code> then the builder is called on the source AST, otherwise it is
	 *            called on the analyzed AST
	 * @return
	 * @throws CompilerException
	 */
	public File callBuilder(File file, String builderName, boolean onSourceAST) throws CompilerException {
		assert file != null;
		assert builderName != null && builderName.length() > 0;

		// prepare the builder input tuple
		IStrategoTerm ast = null;
		if (onSourceAST) {
			ast = ParseService.INSTANCE().parse(new File(Environment.INSTANCE().projectDir, file.getPath()));
		} else {
			assert false : "Builder support on analyzed not yet supported";
			IAnalysisResult analysisResult = AnalysisResultsService.INSTANCE().getResult(file);
			if (analysisResult == null) {
				AnalysisService.INSTANCE().analyze(file);
				analysisResult = AnalysisResultsService.INSTANCE().getResult(file);
			}
			// analysis result cannot be null now
			assert analysisResult != null;
			ast = analysisResult.getAst();
		}
		if (ast == null) {
			throw new CompilerException("Builder " + builderName + "failed. No input AST available.");
		}
		final ITermFactory factory = Environment.INSTANCE().termFactory;
		final IStrategoTerm position = factory.makeList();
		final IStrategoTerm path = factory.makeString(file.getPath());
		final IStrategoTerm projectpath = factory.makeString(Environment.INSTANCE().projectDir.getAbsolutePath());
		final IStrategoTerm inputTuple = factory.makeTuple(ast, position, ast, path, projectpath);
		assert inputTuple != null && inputTuple.getSubtermCount() == 5;

		final ALanguage lang = LanguageService.INSTANCE().getLanguageByExten(file);
		assert lang != null;

		IStrategoTerm result = null;
		result = StrategoCallService.INSTANCE().callStratego(lang, builderName, inputTuple);

		assert result != null : "StrategoCallService returned null. BUG!";
		assert result.getSubtermCount() == 2;
		assert result.getSubterm(0) instanceof IStrategoString;
		assert result.getSubterm(1) instanceof IStrategoString;

		final File resultFile = new File(Environment.INSTANCE().projectDir,
				((IStrategoString) result.getSubterm(0)).stringValue());
		final String resultContents = ((IStrategoString) result.getSubterm(1)).stringValue();
		// write the contents to the file
		try {
			FileUtils.writeStringToFile(resultFile, resultContents);
		} catch (IOException e) {
			throw new CompilerException("Builder " + builderName + "failed to save result", e);
		}

		return resultFile;
	}

}
