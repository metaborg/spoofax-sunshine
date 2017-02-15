package org.metaborg.sunshine.command.base;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.build.BuildInput;
import org.metaborg.core.build.BuildInputBuilder;
import org.metaborg.core.build.CleanInput;
import org.metaborg.core.build.CleanInputBuilder;
import org.metaborg.core.build.dependency.IDependencyService;
import org.metaborg.core.build.paths.ILanguagePathService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.IdentifiedResource;
import org.metaborg.core.messages.StreamMessagePrinter;
import org.metaborg.core.project.IProject;
import org.metaborg.core.source.ISourceTextService;
import org.metaborg.spoofax.core.build.ISpoofaxBuildOutput;
import org.metaborg.spoofax.core.processing.ISpoofaxProcessorRunner;
import org.metaborg.spoofax.core.resource.SpoofaxIgnoresSelector;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.sunshine.arguments.InputDelegate;
import org.metaborg.sunshine.arguments.ProjectPathDelegate;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.core.Tools;

import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.google.common.collect.Iterables;

import spoofax.core.cmd.command.ICommand;

@Parameters(commandDescription = "Analyzes a single file and prints the analyzed AST")
public abstract class AnalyzeCommand implements ICommand {
    private static final ILogger logger = LoggerUtils.logger(AnalyzeCommand.class);

    private final ISourceTextService sourceTextService;
    private final IDependencyService dependencyService;
    private final ILanguagePathService languagePathService;
    private final ISpoofaxProcessorRunner runner;

    private final IStrategoCommon strategoCommon;


    @ParametersDelegate private ProjectPathDelegate projectPathDelegate;
    @ParametersDelegate private InputDelegate inputDelegate;


    public AnalyzeCommand(ISourceTextService sourceTextService, IDependencyService dependencyService,
        ILanguagePathService languagePathService, ISpoofaxProcessorRunner runner, IStrategoCommon strategoCommon,
        ProjectPathDelegate projectPathDelegate, InputDelegate inputDelegate) {
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
            final IProject project = projectPathDelegate.project();
            final IdentifiedResource identifiedResource =
                inputDelegate.inputIdentifiedResource(project.location(), impls);
            final FileObject resource = identifiedResource.resource;
            return run(impls, project, resource);
        } finally {
            projectPathDelegate.removeProject();
        }
    }

    private int run(Iterable<ILanguageImpl> impls, IProject project, FileObject resource) throws MetaborgException {
        try {
            final CleanInputBuilder inputBuilder = new CleanInputBuilder(project);
            // @formatter:off
            final CleanInput input = inputBuilder
                .withSelector(new SpoofaxIgnoresSelector())
                .build(dependencyService)
                ;
            // @formatter:on
            runner.clean(input, null, null).schedule().block();
        } catch(InterruptedException e) {
            logger.error("Clean was cancelled", e);
            return -1;
        }

        // @formatter:off
        final BuildInputBuilder inputBuilder = new BuildInputBuilder(project);
        inputBuilder
            .addLanguages(impls)
            .withDefaultIncludePaths(false)
            .addSource(resource)
            .withMessagePrinter(new StreamMessagePrinter(sourceTextService, true, true, logger))
            .withTransformation(false)
            ;
        // @formatter:on

        final BuildInput input = inputBuilder.build(dependencyService, languagePathService);

        final ISpoofaxAnalyzeUnit result;
        try {
            final ISpoofaxBuildOutput output = runner.build(input, null, null).schedule().block().result();
            if(!output.success()) {
                logger.error("Analysis failed");
                return -1;
            } else {
                final Iterable<ISpoofaxAnalyzeUnit> results = output.analysisResults();
                final int resultSize = Iterables.size(results);
                if(resultSize == 1) {
                    result = Iterables.get(results, 0);
                } else {
                    final String message = logger.format("{} analysis results were returned instead of 1", resultSize);
                    throw new MetaborgException(message);
                }
            }
        } catch(MetaborgRuntimeException e) {
            logger.error("Analysis failed", e);
            return -1;
        } catch(InterruptedException e) {
            logger.error("Analysis was cancelled", e);
            return -1;
        }

        if(!result.hasAst()) {
            System.err.println("Analysis succeeded but produced no AST, printing empty tuple");
            System.out.println("()");
        } else {
            final String ppResult = Tools.asJavaString(strategoCommon.prettyPrint(result.ast()));
            System.out.println(ppResult);
        }

        return 0;
    }
}
