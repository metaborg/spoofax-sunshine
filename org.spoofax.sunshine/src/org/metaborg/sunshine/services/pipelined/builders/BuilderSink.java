/**
 * 
 */
package org.metaborg.sunshine.services.pipelined.builders;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ILanguageIdentifierService;
import org.metaborg.spoofax.core.service.actions.Action;
import org.metaborg.spoofax.core.service.actions.ActionsFacet;
import org.metaborg.sunshine.CompilerException;
import org.metaborg.sunshine.environment.LaunchConfiguration;
import org.metaborg.sunshine.environment.ServiceRegistry;
import org.metaborg.sunshine.pipeline.ISinkOne;
import org.metaborg.sunshine.pipeline.diff.Diff;
import org.metaborg.sunshine.services.StrategoCallService;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class BuilderSink implements ISinkOne<BuilderInputTerm> {
	private static final Logger logger = LogManager.getLogger(BuilderSink.class
			.getName());

	private final String builderName;
	private final LaunchConfiguration lauchConfig;
	private final ILanguageIdentifierService languageIdentifierService;

	public BuilderSink(String builderName, LaunchConfiguration launchConfig,
			ILanguageIdentifierService languageIdentifierService) {
		this.builderName = builderName;
		this.lauchConfig = launchConfig;
		this.languageIdentifierService = languageIdentifierService;
		logger.trace("Created new builder for {}", builderName);
	}

	/**
	 * Call a builder on the file's source or analyzed AST. The builder is
	 * expected to have the
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
	 * In the latter option the assumption being that the builder code itself is
	 * taking care of
	 * writing files to disk if necessary.
	 * 
	 * NB: The current implementation calls the builder on the following input:
	 * 
	 * <code>
	 * 	(ast, [], ast, path, project-path)
	 * </code>
	 * 
	 * NB: The current implementation assumes that the result is a
	 * StrategoString and will not work
	 * with a term.
	 * 
	 * @param product
	 *            The {@link BuilderInputTerm} to run this builder on.
	 * 
	 * @throws CompilerException
	 */
	@Override
	public void sink(Diff<BuilderInputTerm> product) {
		final FileObject file = product.getPayload().getFile();
		final ILanguage language = languageIdentifierService.identify(file);
		final Action action = language.facet(ActionsFacet.class).get(
				builderName);

		if (action == null) {
			logger.fatal("Builder {} could not be found", builderName);
		}
		logger.debug("Invoking builder {} on file {}", action.name, file);
		try {
			IStrategoTerm inputTuple = product.getPayload().toStratego();
			assert inputTuple != null && inputTuple.getSubtermCount() == 5;
			invoke(action, inputTuple);
		} catch (FileSystemException e) {
			final String msg = "Cannot construct input tuple for builder";
			logger.fatal(msg, e);
			throw new CompilerException(msg, e);
		}
	}

	private IStrategoTerm invoke(Action action, IStrategoTerm input) {
		IStrategoTerm result = ServiceRegistry
				.INSTANCE()
				.getService(StrategoCallService.class)
				.callStratego(action.inputLangauge, action.strategoStrategy,
						input);
		processResult(action, result);
		return result;
	}

	private void processResult(Action action, IStrategoTerm result) {
		if (isWriteFile(result)) {
			try {
				final FileObject resultFile = lauchConfig.projectDir
						.resolveFile(((IStrategoString) result.getSubterm(0))
								.stringValue());
				final String resultContents = ((IStrategoString) result
						.getSubterm(1)).stringValue();

				try (OutputStream stream = resultFile.getContent()
						.getOutputStream()) {
					IOUtils.write(resultContents, stream);
				}
			} catch (IOException e) {
				throw new CompilerException("Builder " + action.name
						+ "failed to write result", e);
			}
		}
	}

	private boolean isWriteFile(final IStrategoTerm result) {
		if (result instanceof IStrategoAppl) {
			if (((IStrategoAppl) result).getName().equals("None")) {
				return false;
			} else {
				logger.fatal("Builder returned an unsupported result type {}",
						result);
				throw new CompilerException(
						"Unsupported return value from builder: " + result);
			}
		} else {
			if (result == null || result.getSubtermCount() != 2
					|| !(result.getSubterm(0) instanceof IStrategoString)
					|| !(result.getSubterm(1) instanceof IStrategoString)) {
				logger.fatal("Builder returned an unsupported result type {}",
						result);
				throw new CompilerException(
						"Unsupported return value from builder: " + result);
			} else {
				return true;
			}
		}
	}
}
