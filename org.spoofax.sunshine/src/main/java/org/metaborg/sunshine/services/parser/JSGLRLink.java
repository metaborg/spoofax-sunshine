package org.metaborg.sunshine.services.parser;

import java.io.IOException;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.SpoofaxRuntimeException;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ILanguageIdentifierService;
import org.metaborg.spoofax.core.syntax.ISyntaxService;
import org.metaborg.spoofax.core.syntax.ParseException;
import org.metaborg.spoofax.core.syntax.ParseResult;
import org.metaborg.spoofax.core.text.ISourceTextService;
import org.metaborg.sunshine.environment.ServiceRegistry;
import org.metaborg.sunshine.pipeline.connectors.ALinkOneToOne;
import org.metaborg.sunshine.pipeline.diff.Diff;
import org.metaborg.sunshine.pipeline.diff.DiffKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.common.collect.Iterables;
import com.google.inject.TypeLiteral;

public class JSGLRLink extends ALinkOneToOne<FileObject, ParseResult<IStrategoTerm>> {
    private static final Logger logger = LoggerFactory.getLogger(JSGLRLink.class.getName());

    @Override public Diff<ParseResult<IStrategoTerm>> sinkWork(Diff<FileObject> input) {
        if(input.getDiffKind() == DiffKind.DELETION) {
            logger.debug("File {} has been deleted therefore has no AST", input.getPayload().getName());
            return null;
        }

        final ServiceRegistry serviceRegistry = ServiceRegistry.INSTANCE();
        final FileObject file = input.getPayload();
        final ILanguage language = serviceRegistry.getService(ILanguageIdentifierService.class).identify(file);
        try {
            final String inputText = serviceRegistry.getService(ISourceTextService.class).text(file);
            final ParseResult<IStrategoTerm> parseResult =
                serviceRegistry.getService(new TypeLiteral<ISyntaxService<IStrategoTerm>>() {}).parse(inputText, file,
                    language, null);
            logger.trace("Parsing of file {} produced AST {} and {} messages", input.getPayload(), parseResult.result,
                Iterables.size(parseResult.messages));
            return new Diff<ParseResult<IStrategoTerm>>(parseResult, input.getDiffKind());
        } catch(IOException | ParseException e) {
            final String msg = "Could not parse" + file;
            logger.error(msg, e);
            throw new SpoofaxRuntimeException(msg, e);
        }
    }
}
