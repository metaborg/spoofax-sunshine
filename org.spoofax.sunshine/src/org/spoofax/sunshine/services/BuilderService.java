/**
 * 
 */
package org.spoofax.sunshine.services;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.sunshine.CompilerException;
import org.spoofax.sunshine.Environment;
import org.spoofax.sunshine.model.language.ALanguage;
import org.spoofax.sunshine.parser.impl.SourceAttachment;

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
     * Call a builder on the file's source or analyzed AST. The builder is
     * expected to have the following format:
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
     * NB: The current implementation assumes that the result is a
     * StrategoString and will not work with a term.
     * 
     * @param ast
     *            The ast to call the builder on. The resource (file) backing
     *            the ast is determined from the source attachment (
     *            {@link SourceAttachment#getResource(org.spoofax.interpreter.terms.ISimpleTerm)}
     *            .
     * @param builderName
     *            The name of the builder to call
     * @return
     * @throws CompilerException
     */
    public File callBuilder(IStrategoTerm ast, String builderName)
	    throws CompilerException {
	assert builderName != null && builderName.length() > 0;

	if (ast == null) {
	    throw new CompilerException("Builder " + builderName
		    + "failed. No input AST available.");
	}
	final File file = SourceAttachment.getResource(ast);
	final ITermFactory factory = Environment.INSTANCE().termFactory;
	final IStrategoTerm position = factory.makeList();
	final IStrategoTerm path = factory.makeString(file.getPath());
	final IStrategoTerm projectpath = factory.makeString(Environment
		.INSTANCE().projectDir.getAbsolutePath());
	final IStrategoTerm inputTuple = factory.makeTuple(ast, position, ast,
		path, projectpath);

	assert inputTuple != null && inputTuple.getSubtermCount() == 5;

	final ALanguage lang = LanguageService.INSTANCE().getLanguageByExten(
		file);
	assert lang != null;

	IStrategoTerm result = null;
	result = StrategoCallService.INSTANCE().callStratego(lang, builderName,
		inputTuple);

	assert result != null : "StrategoCallService returned null. BUG!";
	assert result.getSubtermCount() == 2;
	assert result.getSubterm(0) instanceof IStrategoString;
	assert result.getSubterm(1) instanceof IStrategoString;

	final File resultFile = new File(Environment.INSTANCE().projectDir,
		((IStrategoString) result.getSubterm(0)).stringValue());
	final String resultContents = ((IStrategoString) result.getSubterm(1))
		.stringValue();
	// write the contents to the file
	try {
	    FileUtils.writeStringToFile(resultFile, resultContents);
	} catch (IOException e) {
	    throw new CompilerException("Builder " + builderName
		    + "failed to save result", e);
	}

	return resultFile;
    }

}
