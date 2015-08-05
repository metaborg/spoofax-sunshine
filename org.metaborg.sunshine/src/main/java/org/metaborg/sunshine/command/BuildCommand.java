package org.metaborg.sunshine.command;

import java.util.List;

import org.apache.commons.vfs2.FileSelector;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.build.BuildInput;
import org.metaborg.core.build.BuildInputBuilder;
import org.metaborg.core.build.CleanInput;
import org.metaborg.core.build.ConsoleBuildMessagePrinter;
import org.metaborg.core.build.IBuildOutput;
import org.metaborg.core.build.dependency.IDependencyService;
import org.metaborg.core.build.paths.ILanguagePathService;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.project.IProject;
import org.metaborg.core.source.ISourceTextService;
import org.metaborg.core.transform.CompileGoal;
import org.metaborg.core.transform.NamedGoal;
import org.metaborg.spoofax.core.processing.ISpoofaxProcessorRunner;
import org.metaborg.spoofax.core.resource.SpoofaxIgnoresSelector;
import org.metaborg.sunshine.command.arguments.CommonArguments;
import org.metaborg.sunshine.command.arguments.ProjectPathDelegate;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.metaborg.util.resource.FileSelectorUtils;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.google.inject.Inject;

@Parameters(commandDescription = "Parses, analyses, and transforms files in a project")
public class BuildCommand implements ICommand {
    private static final ILogger logger = LoggerUtils.logger(BuildCommand.class);

    // @formatter:off
    @Parameter(names = { "-f", "--filter" }, description = "Regex filter for filtering which files get built") 
    private String allFilterRegex;
    
    @Parameter(names = { "-s", "--stop-on-errors" }, description = "Stops the build when errors occur") 
    private boolean stopOnErrors;
    
    @Parameter(names = { "-A", "--no-analysis" },
        description = "Disables analysis. This will also disable transformation and compilation that requires analysis") 
    private boolean noAnalysis;

    @Parameter(names = { "-T", "--no-transform" }, description = "Disables transformation")
    private boolean noTransform;

    @Parameter(names = { "-t", "--transform-filter" },
        description = "Regex filter for filtering which files get transformed")
    private String transformFilterRegex;

    @Parameter(names = { "-g", "--transform-goal" }, description = "Transform goals (names of builders to invoke)")
    private List<String> namedTransformGoals;

    @Parameter(names = { "-C", "--no-compile-goal" }, description = "Disables compile goal (on-save handler)")
    private boolean noCompile;
    // @formatter:on


    private final ISourceTextService sourceTextService;
    private final IDependencyService dependencyService;
    private final ILanguagePathService languagePathService;
    private final ISpoofaxProcessorRunner runner;

    private final CommonArguments arguments;
    @ParametersDelegate private final ProjectPathDelegate projectPathDelegate;


    @Inject public BuildCommand(ISourceTextService sourceTextService, IDependencyService dependencyService,
        ILanguagePathService languagePathService, ISpoofaxProcessorRunner runner, CommonArguments arguments,
        ProjectPathDelegate projectPathDelegate) {
        this.sourceTextService = sourceTextService;
        this.dependencyService = dependencyService;
        this.languagePathService = languagePathService;
        this.runner = runner;
        this.arguments = arguments;
        this.projectPathDelegate = projectPathDelegate;
    }

    @Override public boolean validate() {
        return true;
    }


    @Override public int run() throws MetaborgException {
        final Iterable<ILanguageComponent> components = arguments.discoverLanguages();
        final IProject project = projectPathDelegate.project();

        try {
            runner.clean(new CleanInput(project, null), null, null).schedule().block();
        } catch(InterruptedException e) {
            logger.error("Clean was cancelled", e);
            return -1;
        }

        // @formatter:off
        final BuildInputBuilder inputBuilder = new BuildInputBuilder(project);
        inputBuilder
            .addComponents(components)
            .withSourcesFromDefaultSourceLocations(true)
            .withMessagePrinter(new ConsoleBuildMessagePrinter(sourceTextService, true, true, logger))
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
                inputBuilder.addTransformGoal(new NamedGoal(name));
            }
        }

        if(!noCompile) {
            inputBuilder.addTransformGoal(new CompileGoal());
        }

        final BuildInput input = inputBuilder.build(dependencyService, languagePathService);

        try {
            final IBuildOutput<?, ?, ?> output = runner.build(input, null, null).schedule().block().result();
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
