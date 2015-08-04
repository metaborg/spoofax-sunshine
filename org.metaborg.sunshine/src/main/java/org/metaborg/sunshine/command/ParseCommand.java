package org.metaborg.sunshine.command;

import java.io.IOException;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.IdentifiedResource;
import org.metaborg.core.language.LanguageUtils;
import org.metaborg.core.source.ISourceTextService;
import org.metaborg.core.syntax.ISyntaxService;
import org.metaborg.core.syntax.ParseResult;
import org.metaborg.spoofax.core.syntax.JSGLRParserConfiguration;
import org.metaborg.spoofax.core.terms.TermPrettyPrinter;
import org.metaborg.sunshine.MessagePrinter;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;

@Parameters
public class ParseCommand implements ICommand {
    private static final ILogger logger = LoggerUtils.logger(ParseCommand.class);

    @Parameter(names = { "-I", "--no-implode" }, description = "Disables imploding the parse tree") private boolean noImplode;

    @Parameter(names = { "-R", "--no-recovery" }, description = "Disables error recovery") private boolean noRecovery;


    private final ISourceTextService sourceTextService;
    private final ISyntaxService<IStrategoTerm> syntaxService;

    private final TermPrettyPrinter termPrettyPrinter;

    private final MessagePrinter printer;

    private final CommonArguments arguments;
    @ParametersDelegate private final InputDelegate inputDelegate;


    @Inject public ParseCommand(ISourceTextService sourceTextService, ISyntaxService<IStrategoTerm> syntaxService,
        TermPrettyPrinter termPrettyPrinter, MessagePrinter printer, CommonArguments arguments,
        InputDelegate inputDelegate) {
        this.sourceTextService = sourceTextService;
        this.syntaxService = syntaxService;
        this.termPrettyPrinter = termPrettyPrinter;
        this.printer = printer;
        this.arguments = arguments;
        this.inputDelegate = inputDelegate;
    }


    @Override public int run() throws MetaborgException {
        final Iterable<ILanguageComponent> components = arguments.discoverLanguages();
        final Iterable<ILanguageImpl> impls = LanguageUtils.toImpls(components);

        final IdentifiedResource identifiedResource = inputDelegate.inputIdentifiedResource(impls);
        final FileObject resource = identifiedResource.resource;

        final String input;
        try {
            input = sourceTextService.text(resource);
        } catch(IOException e) {
            final String message = String.format("Reading %s failed unexpectedly", resource);
            throw new MetaborgException(message, e);
        }

        final ParseResult<IStrategoTerm> result =
            syntaxService.parse(input, resource, identifiedResource.dialectOrLanguage(), new JSGLRParserConfiguration(
                !noImplode, !noRecovery));
        final boolean success = result.result != null;
        final boolean messages = !Iterables.isEmpty(result.messages);
        if(success && messages) {
            logger.error("Parsing succeeded, but messages were produced: ");
            printer.print(result.messages);
        } else if(!success && messages) {
            logger.error("Parsing failed with following messages: ");
            printer.print(result.messages);
            throw new MetaborgException("Parsing failed");
        } else if(!success) {
            logger.error("Parsing failed without messages");
            throw new MetaborgException("Parsing failed");
        }

        final String ppResult = Tools.asJavaString(termPrettyPrinter.prettyPrint(result.result));
        System.out.println(ppResult);

        return 0;
    }
}
