/**
 * 
 */
package org.metaborg.sunshine.dependdriver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metaborg.sunshine.CompilerException;
import org.metaborg.sunshine.environment.LaunchConfiguration;
import org.metaborg.sunshine.environment.ServiceRegistry;
import org.metaborg.sunshine.services.analyzer.AnalysisResult;
import org.metaborg.sunshine.services.analyzer.AnalysisService;
import org.metaborg.sunshine.services.builders.BuilderInputTerm;
import org.metaborg.sunshine.services.builders.IBuilder;
import org.metaborg.sunshine.services.language.ALanguage;
import org.metaborg.sunshine.services.messages.MessageService;
import org.metaborg.sunshine.services.parser.ParserService;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * @author vladvergu
 * 
 */
public class PathToPathDependency implements IActionableDependency {

	private static final Logger logger = LogManager
			.getLogger(PathToPathDependency.class.getName());

	private final IResource input;
	private final IResource output;
	private final ALanguage language;
	private final String builderName;
	private final IResource applyTo;

	public PathToPathDependency(IResource input, IResource output,
			IResource applyTo, ALanguage language, String builderName) {
		Objects.requireNonNull(input, "Null input resource");
		Objects.requireNonNull(output, "Null output resource");
		Objects.requireNonNull(applyTo, "Null applyto resource");
		Objects.requireNonNull(language, "Null language");
		Objects.requireNonNull(builderName, "Null builder name");
		this.input = input;
		this.output = output;
		this.applyTo = applyTo;
		this.language = language;
		this.builderName = builderName;
	}

	@Override
	public boolean execute() {
		Set<Path> entryPoints;
		try {
			entryPoints = applyTo.getFileset();
		} catch (IOException e) {
			logger.fatal("Failed to obtain entry points", e);
			return false;
		}
		logger.debug("Executing {} of language {} on {} files", builderName,
				language.getName(), entryPoints.size());
		for (Path file : entryPoints) {
			if (!executeSingle(file)) {
				return false;
			}
		}
		return true;
	}

	private boolean executeSingle(Path filepath) {
		File file = filepath.toFile();
		logger.trace("Executing {} of language {} on {}", builderName,
				language.getName(), file.getAbsolutePath());
		ServiceRegistry services = ServiceRegistry.INSTANCE();
		MessageService msgService = services.getService(MessageService.class);
		// parse the file
		logger.trace("Parsing " + file);
		AnalysisResult parseResult = services.getService(ParserService.class)
				.parseFile(file, language);
		msgService.addMessages(parseResult.messages());
		if (msgService.hasErrors()) {
			logger.info("Abandoning execution due to parse errors");
			return false;
		}
		// get the builder
		IBuilder builder = language.getBuilder(builderName);
		if (builder == null) {
			logger.fatal("No builder {} for language {}", builderName,
					language.getName());
			throw new CompilerException("No builder " + builderName
					+ " for language " + language.getName());
		}
		IStrategoTerm toBuilderAst;
		if (builder.isOnSource()) {
			toBuilderAst = parseResult.ast();
		} else {
			// analyze the file
			AnalysisService analyzer = services
					.getService(AnalysisService.class);
			logger.trace("Analyzing " + file);
			Collection<AnalysisResult> analyzeResults = analyzer.analyze(Arrays
					.asList(new File[] { file }));
			if (analyzeResults.size() > 1) {
				logger.fatal(
						"INTERNAL ERROR: Can only handle single analysis results but received {}",
						analyzeResults.size());
				throw new CompilerException(
						"INTERNAL ERROR: Can only handle single analysis results but received "
								+ analyzeResults.size());
			}
			AnalysisResult analyzeResult = analyzeResults.iterator().next();
			msgService.addMessages(analyzeResult.messages());
			if (msgService.hasErrors()) {
				logger.info("Abandoning execution due to analysis errors");
				return false;
			}
			toBuilderAst = analyzeResult.ast();
		}
		LaunchConfiguration launch = services
				.getService(LaunchConfiguration.class);
		// construct a builder input term
		BuilderInputTerm builderInput = new BuilderInputTerm(
				launch.termFactory, toBuilderAst, file, launch.projectDir);
		// apply the builder
		logger.trace("Building on " + file);
		builder.invoke(builderInput.toStratego());
		return true;
	}

	@Override
	public boolean isUpdateRequired() {
		try {
			return input.getLastModification() > output.getLastModification();
		} catch (IOException e) {
			logger.warn(
					"Failed to determine whether update is required due to exception",
					e);
		}
		return false;
	}

	@Override
	public boolean isReadyToRun() {
		try {
			return !applyTo.isEmpty();
		} catch (IOException e) {
			logger.warn(
					"Failed to determine whether ready to run due to exception",
					e);
		}
		return false;
	}

}
