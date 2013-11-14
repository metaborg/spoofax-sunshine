/**
 * 
 */
package org.metaborg.sunshine.services.pipelined.builders;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metaborg.sunshine.CompilerException;
import org.metaborg.sunshine.pipeline.ISinkOne;
import org.metaborg.sunshine.pipeline.diff.Diff;
import org.metaborg.sunshine.services.language.LanguageService;
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
		IStrategoTerm inputTuple = product.getPayload().toStratego();
		assert inputTuple != null && inputTuple.getSubtermCount() == 5;
		builder.invoke(inputTuple);

	}
}
