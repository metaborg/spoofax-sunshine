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
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.google.inject.Inject;

@Parameters
public class ParseCommand implements ICommand {
    @Parameter(names = { "-I", "--no-implode" }, description = "Disables imploding the parse tree") private boolean noImplode;

    @Parameter(names = { "-R", "--no-recovery" }, description = "Disables error recovery") private boolean noRecovery;


    private final ISourceTextService sourceTextService;
    private final ISyntaxService<IStrategoTerm> syntaxService;

    private final TermPrettyPrinter termPrettyPrinter;

    private final CommonArguments arguments;
    @ParametersDelegate private final InputDelegate inputDelegate;


    @Inject public ParseCommand(ISourceTextService sourceTextService, ISyntaxService<IStrategoTerm> syntaxService,
        TermPrettyPrinter termPrettyPrinter, CommonArguments arguments, InputDelegate inputDelegate) {
        this.sourceTextService = sourceTextService;
        this.syntaxService = syntaxService;
        this.termPrettyPrinter = termPrettyPrinter;
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

        if(result == null || result.result == null) {
            final String message = String.format("Parsing %s failed, parser returned an empty parse result", resource);
            throw new MetaborgException(message);
        }

        final String ppResult = Tools.asJavaString(termPrettyPrinter.prettyPrint(result.result));
        System.out.println(ppResult);

        return 0;
    }
}
