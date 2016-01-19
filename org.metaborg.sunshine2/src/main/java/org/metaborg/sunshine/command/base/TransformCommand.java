package org.metaborg.sunshine.command.base;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.action.CompileGoal;
import org.metaborg.core.action.EndNamedGoal;
import org.metaborg.core.build.*;
import org.metaborg.core.build.dependency.INewDependencyService;
import org.metaborg.core.build.paths.INewLanguagePathService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.IdentifiedResource;
import org.metaborg.core.project.ILanguageSpec;
import org.metaborg.core.source.ISourceTextService;
import org.metaborg.core.transform.TransformResult;
import org.metaborg.spoofax.core.processing.ISpoofaxProcessorRunner;
import org.metaborg.spoofax.core.resource.SpoofaxIgnoresSelector;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.sunshine.arguments.InputDelegate;
import org.metaborg.sunshine.arguments.LanguageSpecPathDelegate;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;

@Parameters(commandDescription = "Transforms a single file and prints the transformation result")
public abstract class TransformCommand implements ICommand {
    private static final ILogger logger = LoggerUtils.logger(TransformCommand.class);

    // @formatter:off
    @Parameter(names = { "-n", "--named-goal" }, 
        description = "Names transform goal to run (names of builders to invoke). Excludes -c/--compile-goal")
    private String namedGoal;
    
    @Parameter(names = { "-c", "--compile-goal" }, 
        description = "Activates compile goal (on-save handler). Excludes -n/--named-goal")
    private boolean compileGoal;
    // @formatter:on

    private final ISourceTextService sourceTextService;
    private final INewDependencyService dependencyService;
    private final INewLanguagePathService languagePathService;
    private final ISpoofaxProcessorRunner runner;

    private final IStrategoCommon common;

    @ParametersDelegate private final LanguageSpecPathDelegate languageSpecPathDelegate;
    @ParametersDelegate private final InputDelegate inputDelegate;


    @Inject public TransformCommand(ISourceTextService sourceTextService, INewDependencyService dependencyService,
                                    INewLanguagePathService languagePathService, ISpoofaxProcessorRunner runner,
                                    IStrategoCommon strategoTransformerCommon, LanguageSpecPathDelegate languageSpecPathDelegate, InputDelegate inputDelegate) {
        this.sourceTextService = sourceTextService;
        this.dependencyService = dependencyService;
        this.languagePathService = languagePathService;
        this.runner = runner;
        this.common = strategoTransformerCommon;
        this.languageSpecPathDelegate = languageSpecPathDelegate;
        this.inputDelegate = inputDelegate;
    }


    @Override public boolean validate() {
        if(!compileGoal && namedGoal == null) {
            logger.error("Missing goal argument, provide -c/--compile-goal or -n/--named-goal");
            return false;
        } else if(compileGoal && namedGoal != null) {
            logger.error("Choose either -c/--compile-goal or -n/--named-goal, not both");
            return false;
        }
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
            ;
        // @formatter:on

        if(compileGoal) {
            inputBuilder.addTransformGoal(new CompileGoal());
        } else if(namedGoal != null) {
            inputBuilder.addTransformGoal(new EndNamedGoal(namedGoal));
        }

        final BuildInput input = inputBuilder.build(dependencyService, languagePathService);

        final TransformResult<?, IStrategoTerm> result;
        try {
            final IBuildOutput<IStrategoTerm, IStrategoTerm, IStrategoTerm> output =
                runner.build(input, null, null).schedule().block().result();
            if(!output.success()) {
                logger.error("Transformation failed");
                return -1;
            } else {
                final Iterable<TransformResult<IStrategoTerm, IStrategoTerm>> results = output.transformResults();
                final int resultSize = Iterables.size(results);
                if(resultSize == 1) {
                    result = Iterables.get(results, 0);
                } else {
                    throw new MetaborgException(String.format("%s transform results were returned instead of 1",
                        resultSize));
                }
            }
        } catch(MetaborgRuntimeException e) {
            logger.error("Transformation failed", e);
            return -1;
        } catch(InterruptedException e) {
            logger.error("Transformation was cancelled", e);
            return -1;
        }

        final String ppResult = common.toString(result.result);
        System.out.println(ppResult);

        return 0;
    }
}
