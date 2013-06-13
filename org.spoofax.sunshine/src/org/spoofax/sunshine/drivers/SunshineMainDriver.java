/**
 * 
 */
package org.spoofax.sunshine.drivers;

import java.io.File;
import java.io.PrintStream;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.sunshine.CompilerCrashHandler;
import org.spoofax.sunshine.CompilerException;
import org.spoofax.sunshine.Environment;
import org.spoofax.sunshine.model.messages.IMessage;
import org.spoofax.sunshine.model.messages.MessageSeverity;
import org.spoofax.sunshine.pipeline.ILinkManyToMany;
import org.spoofax.sunshine.pipeline.connectors.LinkMapperOneToOne;
import org.spoofax.sunshine.prims.ProjectUtils;
import org.spoofax.sunshine.services.LanguageService;
import org.spoofax.sunshine.services.StrategoCallService;
import org.spoofax.sunshine.services.analyzer.AnalysisResult;
import org.spoofax.sunshine.services.analyzer.AnalyzerLink;
import org.spoofax.sunshine.services.analyzer.legacy.LegacyAnalyzerLink;
import org.spoofax.sunshine.services.filesource.FileSource;
import org.spoofax.sunshine.services.messages.MessageExtractorLink;
import org.spoofax.sunshine.services.messages.MessageSink;
import org.spoofax.sunshine.services.parser.JSGLRLink;
import org.spoofax.sunshine.services.pipelined.builders.BuilderInputTermFactoryLink;
import org.spoofax.sunshine.services.pipelined.builders.BuilderSink;
import org.spoofax.sunshine.statistics.IValidatable;
import org.spoofax.sunshine.statistics.Statistics;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class SunshineMainDriver {
	private static final Logger logger = LogManager.getLogger(SunshineMainDriver.class.getName());

	private MessageSink messageSink;
	private FileSource filesSource;

	public SunshineMainDriver() {
		logger.trace("Initializing & setting uncaught exception handler");
		Thread.currentThread().setUncaughtExceptionHandler(new CompilerCrashHandler());
	}

	protected void emitMessages() {
		final Collection<IMessage> msgs = messageSink.getMessages();
		for (IMessage msg : msgs) {
			@SuppressWarnings("resource")
			PrintStream outStream = msg.severity() == MessageSeverity.ERROR
					|| msg.severity() == MessageSeverity.WARNING ? System.err : System.out;
			outStream.println(msg);
		}
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
		LinkMapperOneToOne<File, AnalysisResult> parserMapper = new LinkMapperOneToOne<File, AnalysisResult>(
				new JSGLRLink());
		logger.trace("Created mapper {} for parser", parserMapper);

		messageSink = new MessageSink();
		filesSource.addSink(parserMapper);
		ILinkManyToMany<AnalysisResult, IMessage> messageSelector = new MessageExtractorLink();
		parserMapper.addSink(messageSelector);
		logger.trace("Message selector {} linked on parse mapper {}", messageSelector, parserMapper);

		messageSelector.addSink(messageSink);

		if (!args.parseonly) {
			if (!args.legacyobserver) {
				ILinkManyToMany<File, AnalysisResult> analyzerLink = null;
				if (!args.noanalysis) {
					analyzerLink = new AnalyzerLink();
					filesSource.addSink(analyzerLink);
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

	public void run() {
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
		emitMessages();
		Statistics.stopTimer();
		Statistics.toNext();
		IStrategoTerm taskData = StrategoCallService.INSTANCE().callStratego(
				LanguageService.INSTANCE().getAnyLanguage(), "task-debug-info",
				Environment.INSTANCE().termFactory.makeTuple());
		System.out
				.println(taskData.getClass() + " " + ((IStrategoList) taskData).getSubtermCount());
	}

}
