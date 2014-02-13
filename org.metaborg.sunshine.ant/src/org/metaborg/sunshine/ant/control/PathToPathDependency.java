/**
 * 
 */
package org.metaborg.sunshine.ant.control;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

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

	private final IDependencyResource input;
	private final IDependencyResource output;
	private final ALanguage language;
	private final String builderName;
	private final IDependencyResource applyTo;

	public PathToPathDependency(IDependencyResource input,
			IDependencyResource output, IDependencyResource applyTo,
			ALanguage language, String builderName) {
		this.input = input;
		this.output = output;
		this.applyTo = applyTo;
		this.language = language;
		this.builderName = builderName;
	}

	@Override
	public boolean execute() {
		File[] entryPoints = applyTo.getFileset();
		logger.trace("Executing {} of language {} on {} files", builderName,
				language.getName(), entryPoints.length);
		for (File file : entryPoints) {
			if (!executeSingle(file)) {
				return false;
			}
		}
		return true;
	}

	private boolean executeSingle(File file) {
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
		return input.getLastModification() > output.getLastModification();
	}

	@Override
	public boolean isReadyToRun() {
		return input.getFileset().length > 0;
	}

}
