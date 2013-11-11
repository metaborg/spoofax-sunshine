/**
 * 
 */
package org.metaborg.sunshine.services.pipelined.builders;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metaborg.sunshine.CompilerException;
import org.metaborg.sunshine.Environment;
import org.metaborg.sunshine.pipeline.ISinkOne;
import org.metaborg.sunshine.pipeline.diff.Diff;
import org.metaborg.sunshine.services.language.LanguageService;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class BuilderSink implements ISinkOne<BuilderInputTerm> {

	private static final Logger logger = LogManager.getLogger(BuilderSink.class.getName());

	private final String builderName;

	public BuilderSink(String builderName) {
		this.builderName = builderName;
		logger.trace("Created new builder for {}", builderName);
	}

	/**
	 * Call a builder on the file's source or analyzed AST. The builder is expected to have the
	 * following format:
	 * 
	 * <code>
	 * builder:
	 * 	(node, position, ast, path, project-path) -> (filename, result)
	 * </code>
	 * 
	 * or
	 * 
	 * <code>
	 * builder:
	 * 	(node, position, ast, path, project-path) -> None()
	 * </code>
	 * 
	 * In the latter option the assumption being that the builder code itself is taking care of
	 * writing files to disk if necessary.
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
	 * @param product
	 *            The {@link BuilderInputTerm} to run this builder on.
	 * 
	 * @throws CompilerException
	 */
	@Override
	public void sink(Diff<BuilderInputTerm> product) {
		IBuilder builder = LanguageService.INSTANCE()
				.getLanguageByExten(product.getPayload().getFile()).getBuilder(builderName);
		if (builder == null) {
			logger.fatal("Builder {} could not be found", builderName);
		}
		logger.debug("Invoking builder {} on file {}", builder.getName(), product.getPayload()
				.getFile());

		File result = applyBuilder(builder, product.getPayload());

		if (result != null)
			logger.info("Builder {} called on file {} and produced file {}", builder.getName(),
					product.getPayload().getFile(), result.getAbsolutePath());
		else
			logger.info(
					"Builder {} called on file {} did not return a file and contents to be written",
					builder.getName(), product.getPayload().getFile());
	}

	private static File applyBuilder(IBuilder builder, BuilderInputTerm input)
			throws CompilerException {
		assert builder != null;

		if (input == null) {
			throw new CompilerException("Builder " + builder.getName()
					+ "failed. No input term available.");
		}
		final IStrategoTerm inputTuple = input.toStratego();
		ensureProperInput(inputTuple);

		IStrategoTerm result = builder.invoke(inputTuple);

		if (isWriteFile(result)) {

			final File resultFile = new File(Environment.INSTANCE().projectDir,
					((IStrategoString) result.getSubterm(0)).stringValue());
			final String resultContents = ((IStrategoString) result.getSubterm(1)).stringValue();
			// write the contents to the file
			try {
				FileUtils.writeStringToFile(resultFile, resultContents);
			} catch (IOException e) {
				throw new CompilerException("Builder " + builder.getName()
						+ "failed to save result", e);
			}
			return resultFile;
		} else {
			return null;
		}
	}

	private static void ensureProperInput(final IStrategoTerm inputTuple) {
		assert inputTuple != null && inputTuple.getSubtermCount() == 5;
	}

	private static boolean isWriteFile(final IStrategoTerm result) {
		if (result instanceof IStrategoAppl) {
			if (((IStrategoAppl) result).getName().equals("None")) {
				return false;
			} else {
				logger.fatal("Builder returned an unsupported result type {}", result);
				throw new CompilerException("Unsupported return value from builder: " + result);
			}
		} else {
			if (result == null || result.getSubtermCount() != 2
					|| !(result.getSubterm(0) instanceof IStrategoString)
					|| !(result.getSubterm(1) instanceof IStrategoString)) {
				logger.fatal("Builder returned an unsupported result type {}", result);
				throw new CompilerException("Unsupported return value from builder: " + result);
			} else {
				return true;
			}
		}
	}

}
