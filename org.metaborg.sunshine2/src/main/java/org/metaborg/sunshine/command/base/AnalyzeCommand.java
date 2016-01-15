package org.metaborg.sunshine.command.base;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.analysis.AnalysisFileResult;
import org.metaborg.core.analysis.AnalysisResult;
import org.metaborg.core.build.*;
import org.metaborg.core.build.dependency.IDependencyService;
import org.metaborg.core.build.dependency.INewDependencyService;
import org.metaborg.core.build.paths.ILanguagePathService;
import org.metaborg.core.build.paths.INewLanguagePathService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.IdentifiedResource;
import org.metaborg.core.project.ILanguageSpec;
import org.metaborg.core.project.IProject;
import org.metaborg.core.source.ISourceTextService;
import org.metaborg.spoofax.core.processing.ISpoofaxProcessorRunner;
import org.metaborg.spoofax.core.resource.SpoofaxIgnoresSelector;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.sunshine.arguments.InputDelegate;
import org.metaborg.sunshine.arguments.LanguageSpecPathDelegate;
import org.metaborg.sunshine.arguments.ProjectPathDelegate;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.google.common.collect.Iterables;

@Parameters(commandDescription = "Analyzes a single file and prints the analyzed AST")
public abstract class AnalyzeCommand implements ICommand {
    private static final ILogger logger = LoggerUtils.logger(AnalyzeCommand.class);

    private final ISourceTextService sourceTextService;
    private final INewDependencyService dependencyService;
    private final INewLanguagePathService languagePathService;
    private final ISpoofaxProcessorRunner runner;

    private final IStrategoCommon strategoCommon;


    @ParametersDelegate private final LanguageSpecPathDelegate languageSpecPathDelegate;
    @ParametersDelegate private final InputDelegate inputDelegate;


    public AnalyzeCommand(ISourceTextService sourceTextService, INewDependencyService dependencyService,
                          INewLanguagePathService languagePathService, ISpoofaxProcessorRunner runner, IStrategoCommon strategoCommon,
                          LanguageSpecPathDelegate languageSpecPathDelegate, InputDelegate inputDelegate) {
        this.sourceTextService = sourceTextService;
        this.dependencyService = dependencyService;
        this.languagePathService = languagePathService;
        this.runner = runner;
        this.strategoCommon = strategoCommon;
        this.languageSpecPathDelegate = languageSpecPathDelegate;
        this.inputDelegate = inputDelegate;
    }


    @Override public boolean validate() {
        return true;
    }

    protected int run(Iterable<ILanguageImpl> impls) throws MetaborgException {
        try {
            final ILanguageSpec languageSpec = languageSpecPathDelegate.languageSpec();
            final IdentifiedResource identifiedResource =
                inputDelegate.inputIdentifiedResource(languageSpec.location(), impls);
            final FileObject resource = identifiedResource.resource;
            return run(impls, languageSpec, resource);
        } finally {
            languageSpecPathDelegate.removeProject();
        }
    }

    private int run(Iterable<ILanguageImpl> impls, ILanguageSpec languageSpec, FileObject resource) throws MetaborgException {
//    private int run(Iterable<ILanguageImpl> impls, IProject project, FileObject resource) throws MetaborgException {
        try {
            final CleanInputBuilder inputBuilder = new CleanInputBuilder(languageSpec);
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
        final NewBuildInputBuilder inputBuilder = new NewBuildInputBuilder(languageSpec);
        inputBuilder
            .addLanguages(impls)
            .withDefaultIncludePaths(false)
            .addSource(resource)
            .withMessagePrinter(new ConsoleBuildMessagePrinter(sourceTextService, true, true, logger))
            .withTransformation(false)
            ;
        // @formatter:on

        final BuildInput input = inputBuilder.build(dependencyService, languagePathService);

        final AnalysisFileResult<IStrategoTerm, IStrategoTerm> fileResult;
        try {
            final IBuildOutput<IStrategoTerm, IStrategoTerm, IStrategoTerm> output =
                runner.build(input, null, null).schedule().block().result();
            if(!output.success()) {
                logger.error("Analysis failed");
                return -1;
            } else {
                final Iterable<AnalysisResult<IStrategoTerm, IStrategoTerm>> results = output.analysisResults();
                final int resultSize = Iterables.size(results);
                if(resultSize == 1) {
                    final AnalysisResult<IStrategoTerm, IStrategoTerm> result = Iterables.get(results, 0);
                    final int fileResultSize = Iterables.size(result.fileResults);
                    if(fileResultSize == 1) {
                        fileResult = Iterables.get(result.fileResults, 0);
                    } else {
                        throw new MetaborgException(String.format(
                            "%s analysis file results were returned instead of 1", fileResultSize));
                    }
                } else {
                    throw new MetaborgException(String.format("%s analysis results were returned instead of 1",
                        resultSize));
                }
            }
        } catch(MetaborgRuntimeException e) {
            logger.error("Analysis failed", e);
            return -1;
        } catch(InterruptedException e) {
            logger.error("Analysis was cancelled", e);
            return -1;
        }

        final String ppResult = Tools.asJavaString(strategoCommon.prettyPrint(fileResult.result));
        System.out.println(ppResult);

        return 0;
    }
}
