/**
 * 
 */
package org.metaborg.sunshine.services.parser;

import java.io.IOException;

import org.apache.commons.vfs2.FileObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metaborg.spoofax.core.SpoofaxException;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ILanguageIdentifierService;
import org.metaborg.spoofax.core.syntax.ISyntaxService;
import org.metaborg.spoofax.core.syntax.ParseResult;
import org.metaborg.sunshine.environment.ServiceRegistry;
import org.metaborg.sunshine.pipeline.connectors.ALinkOneToOne;
import org.metaborg.sunshine.pipeline.diff.Diff;
import org.metaborg.sunshine.pipeline.diff.DiffKind;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.TypeLiteral;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class JSGLRLink extends
		ALinkOneToOne<FileObject, ParseResult<IStrategoTerm>> {

	private static final Logger logger = LogManager.getLogger(JSGLRLink.class
			.getName());

	@Override
	public Diff<ParseResult<IStrategoTerm>> sinkWork(Diff<FileObject> input) {
		if (input.getDiffKind() == DiffKind.DELETION) {
			logger.debug("File {} has been deleted therefore has no AST", input
					.getPayload().getName());
			return null;
		}

		final ServiceRegistry serviceRegistry = ServiceRegistry.INSTANCE();
		final FileObject file = input.getPayload();
		final ILanguage language = serviceRegistry.getService(
				ILanguageIdentifierService.class).identify(file);
		try {
			final ParseResult<IStrategoTerm> parseResult = serviceRegistry
					.getService(
							new TypeLiteral<ISyntaxService<IStrategoTerm>>() {
							}).parse(file,
							language);
			logger.trace("Parsing of file {} produced AST {} and {} messages",
					input.getPayload(), parseResult.result,
					parseResult.messages.size());
			return new Diff<ParseResult<IStrategoTerm>>(parseResult,
					input.getDiffKind());
		} catch (IOException e) {
			final String msg = "Could not parse" + file;
			logger.fatal(msg, e);
			throw new SpoofaxException(msg, e);
		}
	}

}
