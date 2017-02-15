package org.metaborg.sunshine.command.base;

import java.util.List;

import org.apache.commons.vfs2.FileSelector;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.action.CompileGoal;
import org.metaborg.core.action.EndNamedGoal;
import org.metaborg.core.build.BuildInput;
import org.metaborg.core.build.BuildInputBuilder;
import org.metaborg.core.build.CleanInput;
import org.metaborg.core.build.CleanInputBuilder;
import org.metaborg.core.build.dependency.IDependencyService;
import org.metaborg.core.build.paths.ILanguagePathService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.messages.StreamMessagePrinter;
import org.metaborg.core.project.IProject;
import org.metaborg.core.source.ISourceTextService;
import org.metaborg.spoofax.core.build.ISpoofaxBuildOutput;
import org.metaborg.spoofax.core.processing.ISpoofaxProcessorRunner;
import org.metaborg.spoofax.core.resource.SpoofaxIgnoresSelector;
import org.metaborg.sunshine.arguments.ProjectPathDelegate;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.metaborg.util.resource.FileSelectorUtils;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.google.inject.Inject;

import spoofax.core.cmd.command.ICommand;

@Parameters(commandDescription = "Parses, analyses, and transforms files in a project")
public abstract class BuildCommand implements ICommand {
    private static final ILogger logger = LoggerUtils.logger(BuildCommand.class);

    // @formatter:off
    @Parameter(names = { "-f", "--filter" }, description = "Regex filter for filtering which files get built") 
    private String allFilterRegex;
    
    @Parameter(names = { "-s", "--stop-on-errors" }, description = "Stops the build when errors occur") 
    private boolean stopOnErrors;
    
    @Parameter(names = { "-A", "--no-analysis" }, hidden = true, 
        description = "Disables analysis. This will also disable transformation and compilation that requires analysis") 
    private boolean noAnalysis;

    @Parameter(names = { "-T", "--no-transform" }, description = "Disables transformation")
    private boolean noTransform;

    @Parameter(names = { "-t", "--transform-filter" }, hidden = true, 
        description = "Regex filter for filtering which files get transformed")
    private String transformFilterRegex;

    @Parameter(names = { "-n", "--transform-goal" }, 
        description = "Named transform goals to run (names of builders to invoke)")
    private List<String> namedTransformGoals;

    @Parameter(names = { "-C", "--no-compile-goal" }, description = "Disables compile goal (on-save handler)")
    private boolean noCompile;
    // @formatter:on


    private final ISourceTextService sourceTextService;
    private final IDependencyService dependencyService;
    private final ILanguagePathService languagePathService;
    private final ISpoofaxProcessorRunner runner;

    @ParametersDelegate private ProjectPathDelegate projectPathDelegate;


    @Inject public BuildCommand(ISourceTextService sourceTextService, IDependencyService dependencyService,
        ILanguagePathService languagePathService, ISpoofaxProcessorRunner runner,
        ProjectPathDelegate projectPathDelegate) {
        this.sourceTextService = sourceTextService;
        this.dependencyService = dependencyService;
        this.languagePathService = languagePathService;
        this.runner = runner;
        this.projectPathDelegate = projectPathDelegate;
    }

    @Override public boolean validate() {
        return true;
    }


    protected int run(Iterable<ILanguageImpl> impls) throws MetaborgException {
        try {
            final IProject project = projectPathDelegate.project();
            return run(impls, project);
        } finally {
            projectPathDelegate.removeProject();
        }
    }

    private int run(Iterable<ILanguageImpl> impls, IProject project) throws MetaborgException {
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
            .withSourcesFromDefaultSourceLocations(true)
            .withMessagePrinter(new StreamMessagePrinter(sourceTextService, true, true, logger))
            .withThrowOnErrors(stopOnErrors)
            .withAnalysis(!noAnalysis)
            .withTransformation(!noTransform)
            ;
        // @formatter:on

        final FileSelector ignoresSelector = new SpoofaxIgnoresSelector();
        if(allFilterRegex != null) {
            inputBuilder.withSelector(FileSelectorUtils.and(ignoresSelector, FileSelectorUtils.regex(allFilterRegex)));
        } else {
            inputBuilder.withSelector(ignoresSelector);
        }

        if(transformFilterRegex != null) {
            inputBuilder.withTransformSelector(FileSelectorUtils.regex(transformFilterRegex));
        }

        if(namedTransformGoals != null) {
            for(String name : namedTransformGoals) {
                inputBuilder.addTransformGoal(new EndNamedGoal(name));
            }
        }

        if(!noCompile) {
            inputBuilder.addTransformGoal(new CompileGoal());
        }

        final BuildInput input = inputBuilder.build(dependencyService, languagePathService);

        try {
            final ISpoofaxBuildOutput output = runner.build(input, null, null).schedule().block().result();
            if(!output.success()) {
                logger.error("Build failed");
                return -1;
            } else {
                logger.info("Build successful");
            }
        } catch(MetaborgRuntimeException e) {
            logger.error("Build failed", e);
            return -1;
        } catch(InterruptedException e) {
            logger.error("Build was cancelled", e);
            return -1;
        }

        return 0;
    }
}
