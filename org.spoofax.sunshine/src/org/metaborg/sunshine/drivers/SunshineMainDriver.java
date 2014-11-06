/**
 * 
 */
package org.metaborg.sunshine.drivers;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.apache.commons.vfs2.FileObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metaborg.spoofax.core.language.AllLanguagesFileSelector;
import org.metaborg.spoofax.core.language.ILanguageIdentifierService;
import org.metaborg.spoofax.core.language.ILanguageService;
import org.metaborg.spoofax.core.messages.IMessage;
import org.metaborg.spoofax.core.parser.ParseResult;
import org.metaborg.sunshine.CompilerCrashHandler;
import org.metaborg.sunshine.CompilerException;
import org.metaborg.sunshine.environment.LaunchConfiguration;
import org.metaborg.sunshine.environment.SunshineMainArguments;
import org.metaborg.sunshine.model.messages.MessageEmitter;
import org.metaborg.sunshine.pipeline.ILinkManyToMany;
import org.metaborg.sunshine.pipeline.connectors.ALinkManyToMany;
import org.metaborg.sunshine.pipeline.connectors.LinkMapperOneToOne;
import org.metaborg.sunshine.prims.ProjectUtils;
import org.metaborg.sunshine.services.analyzer.AnalysisFileResult;
import org.metaborg.sunshine.services.analyzer.AnalyzerLink;
import org.metaborg.sunshine.services.analyzer.legacy.LegacyAnalyzerLink;
import org.metaborg.sunshine.services.filesource.FileSource;
import org.metaborg.sunshine.services.filesource.FileSourceFilter;
import org.metaborg.sunshine.services.messages.MessageExtractorLink;
import org.metaborg.sunshine.services.messages.MessageSink;
import org.metaborg.sunshine.services.parser.JSGLRLink;
import org.metaborg.sunshine.services.parser.ParseToAnalysisResultLink;
import org.metaborg.sunshine.services.pipelined.builders.BuilderInputTermFactoryLink;
import org.metaborg.sunshine.services.pipelined.builders.BuilderSink;
import org.metaborg.sunshine.statistics.IValidatable;
import org.metaborg.sunshine.statistics.Statistics;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Inject;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class SunshineMainDriver {
	private static final Logger logger = LogManager
			.getLogger(SunshineMainDriver.class.getName());

	private final LaunchConfiguration launchConfig;
	private final ILanguageService languageService;
	private final ILanguageIdentifierService languageIdentifierService;

	private MessageEmitter emitter;
	private FileSource filesSource;

	@Inject
	public SunshineMainDriver(LaunchConfiguration launchConfig,
			ILanguageService languageService,
			ILanguageIdentifierService languageIdentifierService) {
		this.launchConfig = launchConfig;
		this.languageService = languageService;
		this.languageIdentifierService = languageIdentifierService;

		logger.trace("Initializing & setting uncaught exception handler");
		Thread.currentThread().setUncaughtExceptionHandler(
				new CompilerCrashHandler());
	}

	public void init() throws CompilerException {
		logger.trace("Beginning init");
		if (launchConfig.mainArguments.nonincremental) {
			ProjectUtils.cleanProject();
			ProjectUtils.unloadTasks();
			ProjectUtils.unloadIndex();
		}
		logger.trace("Init completed");
	}

	private void initPipeline() throws IOException {
		logger.debug("Initializing pipeline");
		SunshineMainArguments args = launchConfig.mainArguments;

		Statistics.startTimer("PIPELINE_CONSTRUCT");
		filesSource = new FileSource(launchConfig.projectDir, languageService);
		logger.trace("Created file source {}", filesSource);
		FileSourceFilter fsf = new FileSourceFilter(args.filefilter);
		filesSource.addSink(fsf);
		logger.trace("Created file source filter {}", fsf);
		LinkMapperOneToOne<FileObject, ParseResult<IStrategoTerm>> parserMapper = new LinkMapperOneToOne<FileObject, ParseResult<IStrategoTerm>>(
				new JSGLRLink());
		logger.trace("Created mapper {} for parser", parserMapper);
		ALinkManyToMany<ParseResult<IStrategoTerm>, AnalysisFileResult> parseToAnalysisResultMapper = new ParseToAnalysisResultLink();
		parserMapper.addSink(parseToAnalysisResultMapper);

		MessageSink messageSink = new MessageSink();
		emitter = new MessageEmitter(messageSink);
		fsf.addSink(parserMapper);
		ILinkManyToMany<AnalysisFileResult, IMessage> messageSelector = new MessageExtractorLink();
		parseToAnalysisResultMapper.addSink(messageSelector);
		logger.trace("Message selector {} linked on parse mapper {}",
				messageSelector, parserMapper);

		messageSelector.addSink(messageSink);

		if (!args.parseonly) {
			if (!args.legacyobserver) {
				ILinkManyToMany<ParseResult<IStrategoTerm>, AnalysisFileResult> analyzerLink = null;
				if (!args.noanalysis) {
					analyzerLink = new AnalyzerLink();
					parserMapper.addSink(analyzerLink);
					analyzerLink.addSink(messageSelector);
				}

				if (args.builder != null) {
					for (FileObject file : getFilesToBuild()) {
						logger.trace("Creating builder links for builder {}",
								args.builder);
						BuilderInputTermFactoryLink inputMakeLink = new BuilderInputTermFactoryLink(
								file, args.noanalysis || args.buildonsource,
								args.buildwitherrors);
						BuilderSink compileBuilder = new BuilderSink(
								args.builder, launchConfig,
								languageIdentifierService);
						logger.trace("Wiring builder up into pipeline");
						if (!args.noanalysis)
							analyzerLink.addSink(inputMakeLink);
						else
							parseToAnalysisResultMapper.addSink(inputMakeLink);
						inputMakeLink.addSink(compileBuilder);
					}
				}
			} else {
				LinkMapperOneToOne<ParseResult<IStrategoTerm>, AnalysisFileResult> analyzerMapper = null;
				if (!args.noanalysis) {
					analyzerMapper = new LinkMapperOneToOne<ParseResult<IStrategoTerm>, AnalysisFileResult>(
							new LegacyAnalyzerLink(languageIdentifierService));
					parserMapper.addSink(analyzerMapper);
					ILinkManyToMany<AnalysisFileResult, IMessage> messageSelector2 = new MessageExtractorLink();
					analyzerMapper.addSink(messageSelector2);
					messageSelector2.addSink(messageSink);
				}
				if (args.builder != null) {
					for (FileObject file : getFilesToBuild()) {
						logger.trace("Creating builder links for builder {}",
								args.builder);
						BuilderInputTermFactoryLink inputMakeLink = new BuilderInputTermFactoryLink(
								file, args.noanalysis || args.buildonsource,
								args.buildwitherrors);
						BuilderSink compileBuilder = new BuilderSink(
								args.builder, launchConfig,
								languageIdentifierService);
						logger.trace("Wiring builder up into pipeline");

						if (args.noanalysis) {
							parseToAnalysisResultMapper.addSink(inputMakeLink);
						} else {
							analyzerMapper.addSink(inputMakeLink);
						}

						inputMakeLink.addSink(compileBuilder);
					}
				}
			}

		}

		Statistics.stopTimer();

		logger.info("Pipeline initialized");
	}

	private Collection<FileObject> getFilesToBuild() throws IOException {
		SunshineMainArguments args = launchConfig.mainArguments;
		Collection<FileObject> files = new LinkedList<FileObject>();
		if (args.filetobuildon != null) {
			final FileObject buildTargetFile = launchConfig.projectDir
					.resolveFile(args.filetobuildon);
			if (!buildTargetFile.exists()) {
				throw new CompilerException("File not found: "
						+ args.filetobuildon);
			}
			files.add(buildTargetFile);
		}
		if (args.filestobuildon != null) {
			final FileObject directory = launchConfig.projectDir
					.resolveFile(args.filestobuildon);
			final FileObject[] languageFiles = directory
					.findFiles(new AllLanguagesFileSelector(
							languageIdentifierService));
			Collections.addAll(files, languageFiles);
		}
		if (files.size() == 0 && args.filestobuildon != null) {
			throw new CompilerException("No files found matching: "
					+ args.filestobuildon);
		}
		return files;
	}

	public int run() throws IOException {
		Statistics.startTimer("RUN");
		logger.debug("Beginning run");
		init();
		initPipeline();
		Statistics
				.addDataPoint(
						"INCREMENTAL",
						launchConfig.mainArguments.nonincremental ? IValidatable.NEVER_VALIDATABLE
								: IValidatable.ALWAYS_VALIDATABLE);
		logger.trace("Beginning pushing file changes");
		Statistics.startTimer("POKE");
		filesSource.poke();
		Statistics.stopTimer();
		logger.trace("Emitting messages");
		emitter.emitMessages(System.out);
		emitter.emitSummary(System.out);
		Statistics.stopTimer();
		Statistics.toNext();
		return !emitter.hasErrors() ? 0 : 1;
	}

}
