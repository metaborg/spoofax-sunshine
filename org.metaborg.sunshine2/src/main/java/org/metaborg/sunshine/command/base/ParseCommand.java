package org.metaborg.sunshine.command.base;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.build.*;
import org.metaborg.core.build.dependency.IDependencyService;
import org.metaborg.core.build.paths.ILanguagePathService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.IdentifiedResource;
import org.metaborg.core.project.ILanguageSpec;
import org.metaborg.core.source.ISourceTextService;
import org.metaborg.core.syntax.ParseResult;
import org.metaborg.spoofax.core.processing.ISpoofaxProcessorRunner;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.sunshine.arguments.InputDelegate;
import org.metaborg.sunshine.arguments.LanguageSpecPathDelegate;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;

@Parameters(commandDescription = "Parses a single file and prints the parsed AST")
public abstract class ParseCommand implements ICommand {
    private static final ILogger logger = LoggerUtils.logger(ParseCommand.class);

    // @formatter:off
    @Parameter(names = { "-I", "--no-implode" }, hidden = true, description = "Disables imploding the parse tree")
    private boolean noImplode;

    @Parameter(names = { "-R", "--no-recovery" }, hidden = true, description = "Disables error recovery")
    private boolean noRecovery;
    // @formatter:on

    private final ISourceTextService sourceTextService;
    private final IDependencyService dependencyService;
    private final ILanguagePathService languagePathService;
    private final ISpoofaxProcessorRunner runner;

    private final IStrategoCommon strategoCommon;

    @ParametersDelegate private final LanguageSpecPathDelegate projectPathDelegate;
    @ParametersDelegate private final InputDelegate inputDelegate;


    @Inject public ParseCommand(ISourceTextService sourceTextService, IDependencyService dependencyService,
                                ILanguagePathService languagePathService, ISpoofaxProcessorRunner runner, IStrategoCommon strategoCommon,
                                LanguageSpecPathDelegate projectPathDelegate, InputDelegate inputDelegate) {
        this.sourceTextService = sourceTextService;
        this.dependencyService = dependencyService;
        this.languagePathService = languagePathService;
        this.runner = runner;
        this.strategoCommon = strategoCommon;
        this.projectPathDelegate = projectPathDelegate;
        this.inputDelegate = inputDelegate;
    }


    @Override public boolean validate() {
        return true;
    }

    protected int run(Iterable<ILanguageImpl> impls) throws MetaborgException {
        try {
            final ILanguageSpec languageSpec = projectPathDelegate.languageSpec();
            final IdentifiedResource identifiedResource =
                inputDelegate.inputIdentifiedResource(languageSpec.location(), impls);
            final FileObject resource = identifiedResource.resource;
            return run(impls, languageSpec, resource);
        } finally {
            projectPathDelegate.removeProject();
        }
    }

    private int run(Iterable<ILanguageImpl> impls, ILanguageSpec languageSpec, FileObject resource) throws MetaborgException {
        // @formatter:off
        final BuildInputBuilder inputBuilder = new BuildInputBuilder(languageSpec);
        inputBuilder
            .addLanguages(impls)
            .withDefaultIncludePaths(false)
            .addSource(resource)
            .withMessagePrinter(new ConsoleBuildMessagePrinter(sourceTextService, true, true, logger))
            .withAnalysis(false)
            .withTransformation(false)
            ;
        // @formatter:on

        final BuildInput input = inputBuilder.build(dependencyService, languagePathService);

        final ParseResult<IStrategoTerm> result;
        try {
            final IBuildOutput<IStrategoTerm, IStrategoTerm, IStrategoTerm> output =
                runner.build(input, null, null).schedule().block().result();
            if(!output.success()) {
                logger.error("Parsing failed");
                return -1;
            } else {
                final Iterable<ParseResult<IStrategoTerm>> results = output.parseResults();
                final int resultSize = Iterables.size(results);
                if(resultSize == 1) {
                    result = Iterables.get(results, 0);
                } else {
                    throw new MetaborgException(
                        String.format("%s parse results were returned instead of 1", resultSize));
                }
            }
        } catch(MetaborgRuntimeException e) {
            logger.error("Parsing failed", e);
            return -1;
        } catch(InterruptedException e) {
            logger.error("Parsing was cancelled", e);
            return -1;
        }

        final String ppResult = Tools.asJavaString(strategoCommon.prettyPrint(result.result));
        System.out.println(ppResult);

        return 0;
    }
}
