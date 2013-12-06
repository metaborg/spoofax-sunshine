/**
 * 
 */
package org.metaborg.sunshine.drivers;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metaborg.sunshine.CompilerCrashHandler;
import org.metaborg.sunshine.CompilerException;
import org.metaborg.sunshine.Environment;
import org.metaborg.sunshine.model.messages.IMessage;
import org.metaborg.sunshine.model.messages.MessageEmitter;
import org.metaborg.sunshine.pipeline.ILinkManyToMany;
import org.metaborg.sunshine.pipeline.connectors.LinkMapperOneToOne;
import org.metaborg.sunshine.prims.ProjectUtils;
import org.metaborg.sunshine.services.analyzer.AnalysisResult;
import org.metaborg.sunshine.services.analyzer.AnalyzerLink;
import org.metaborg.sunshine.services.analyzer.legacy.LegacyAnalyzerLink;
import org.metaborg.sunshine.services.filesource.FileSource;
import org.metaborg.sunshine.services.filesource.FileSourceFilter;
import org.metaborg.sunshine.services.language.LanguageService;
import org.metaborg.sunshine.services.messages.MessageExtractorLink;
import org.metaborg.sunshine.services.messages.MessageSink;
import org.metaborg.sunshine.services.parser.JSGLRLink;
import org.metaborg.sunshine.services.pipelined.builders.BuilderInputTermFactoryLink;
import org.metaborg.sunshine.services.pipelined.builders.BuilderSink;
import org.metaborg.sunshine.statistics.IValidatable;
import org.metaborg.sunshine.statistics.Statistics;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class SunshineMainDriver {
	private static final Logger logger = LogManager.getLogger(SunshineMainDriver.class.getName());

	private MessageEmitter emitter;
	private FileSource filesSource;

	public SunshineMainDriver() {
		logger.trace("Initializing & setting uncaught exception handler");
		Thread.currentThread().setUncaughtExceptionHandler(new CompilerCrashHandler());
	}

	public void init() throws CompilerException {
		logger.trace("Beginning init");
		if (Environment.INSTANCE().getMainArguments().nonincremental) {
			ProjectUtils.cleanProject();
			ProjectUtils.unloadTasks();
			ProjectUtils.unloadIndex();
		}
		logger.trace("Init completed");
	}

	private void initPipeline() {
		logger.debug("Initializing pipeline");
		Environment env = Environment.INSTANCE();
		SunshineMainArguments args = env.getMainArguments();

		Statistics.startTimer("PIPELINE_CONSTRUCT");
		filesSource = new FileSource(env.projectDir);
		logger.trace("Created file source {}", filesSource);
		FileSourceFilter fsf = new FileSourceFilter(args.filefilter);
		filesSource.addSink(fsf);
		logger.trace("Created file source filter {}", fsf);
		LinkMapperOneToOne<File, AnalysisResult> parserMapper = new LinkMapperOneToOne<File, AnalysisResult>(
				new JSGLRLink());
		logger.trace("Created mapper {} for parser", parserMapper);

		MessageSink messageSink = new MessageSink();
		emitter = new MessageEmitter(messageSink);
		fsf.addSink(parserMapper);
		ILinkManyToMany<AnalysisResult, IMessage> messageSelector = new MessageExtractorLink();
		parserMapper.addSink(messageSelector);
		logger.trace("Message selector {} linked on parse mapper {}", messageSelector, parserMapper);

		messageSelector.addSink(messageSink);

		if (!args.parseonly) {
			if (!args.legacyobserver) {
				ILinkManyToMany<File, AnalysisResult> analyzerLink = null;
				if (!args.noanalysis) {
					analyzerLink = new AnalyzerLink();
					fsf.addSink(analyzerLink);
					analyzerLink.addSink(messageSelector);
				}

				if (args.builder != null) {
					for (File file : getFilesToBuild()) {
						logger.trace("Creating builder links for builder {}", args.builder);
						BuilderInputTermFactoryLink inputMakeLink = new BuilderInputTermFactoryLink(
								file, args.noanalysis || args.buildonsource, args.buildwitherrors);
						BuilderSink compileBuilder = new BuilderSink(args.builder);
						logger.trace("Wiring builder up into pipeline");
						if (!args.noanalysis)
							analyzerLink.addSink(inputMakeLink);
						else
							parserMapper.addSink(inputMakeLink);
						inputMakeLink.addSink(compileBuilder);
					}
				}
			} else {
				LinkMapperOneToOne<AnalysisResult, AnalysisResult> analyzerMapper = null;
				if (!args.noanalysis) {
					analyzerMapper = new LinkMapperOneToOne<AnalysisResult, AnalysisResult>(
							new LegacyAnalyzerLink());
					parserMapper.addSink(analyzerMapper);
					ILinkManyToMany<AnalysisResult, IMessage> messageSelector2 = new MessageExtractorLink();
					analyzerMapper.addSink(messageSelector2);
					messageSelector2.addSink(messageSink);
				}
				if (args.builder != null) {
					for (File file : getFilesToBuild()) {
						logger.trace("Creating builder links for builder {}", args.builder);
						BuilderInputTermFactoryLink inputMakeLink = new BuilderInputTermFactoryLink(
								file, args.noanalysis || args.buildonsource, args.buildwitherrors);
						BuilderSink compileBuilder = new BuilderSink(args.builder);
						logger.trace("Wiring builder up into pipeline");

						if (args.noanalysis) {
							parserMapper.addSink(inputMakeLink);
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

	private Collection<File> getFilesToBuild() {
		Environment env = Environment.INSTANCE();
		SunshineMainArguments args = env.getMainArguments();
		Collection<File> files = new LinkedList<File>();
		if (args.filetobuildon != null) {
			files.add(new File(env.projectDir, args.filetobuildon));
		}
		if (args.filestobuildon != null) {
			files.addAll(FileUtils.listFiles(
					new File(env.projectDir, args.filestobuildon),
					LanguageService
							.INSTANCE()
							.getSupportedExtens()
							.toArray(
									new String[LanguageService.INSTANCE().getSupportedExtens()
											.size()]), true));
		}
		return files;
	}

	public int run() {
		Statistics.startTimer("RUN");
		logger.debug("Beginning run");
		init();
		initPipeline();
		Statistics
				.addDataPoint(
						"INCREMENTAL",
						Environment.INSTANCE().getMainArguments().nonincremental ? IValidatable.NEVER_VALIDATABLE
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
