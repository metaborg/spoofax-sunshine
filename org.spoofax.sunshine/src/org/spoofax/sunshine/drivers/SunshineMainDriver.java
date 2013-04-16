/**
 * 
 */
package org.spoofax.sunshine.drivers;

import java.io.File;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spoofax.sunshine.CompilerCrashHandler;
import org.spoofax.sunshine.CompilerException;
import org.spoofax.sunshine.Environment;
import org.spoofax.sunshine.LaunchConfiguration;
import org.spoofax.sunshine.model.messages.IMessage;
import org.spoofax.sunshine.parser.model.IStrategoParseOrAnalyzeResult;
import org.spoofax.sunshine.pipeline.ILinkManyToMany;
import org.spoofax.sunshine.pipeline.ILinkOneToOne;
import org.spoofax.sunshine.pipeline.connectors.LinkMapperOneToOne;
import org.spoofax.sunshine.pipeline.services.AnalyzerLink;
import org.spoofax.sunshine.pipeline.services.FileSource;
import org.spoofax.sunshine.pipeline.services.JSGLRLink;
import org.spoofax.sunshine.pipeline.services.MessageExtractorLink;
import org.spoofax.sunshine.pipeline.services.MessageSink;
import org.spoofax.sunshine.services.LanguageService;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class SunshineMainDriver {
    private static final Logger logger = LogManager
	    .getLogger(SunshineMainDriver.class.getName());

    private MessageSink messageSink;
    private FileSource filesSource;

    public SunshineMainDriver() {
	logger.trace("Initializing & setting uncaught exception handler");
	Thread.currentThread().setUncaughtExceptionHandler(
		new CompilerCrashHandler());
    }

    protected void emitMessages() {
	final Collection<IMessage> msgs = messageSink.getMessages();
	for (IMessage msg : msgs) {
	    System.err.println(msg);
	}
    }

    public void init() throws CompilerException {
	logger.trace("Beginning init");
	final LaunchConfiguration config = Environment.INSTANCE()
		.getLaunchConfiguration();
	LanguageService.INSTANCE().registerLanguage(config.languages);
	Environment.INSTANCE().setProjectDir(new File(config.project_dir));
	logger.trace("Init completed");
    }

    private void initPipeline() {
	logger.debug("Initializing pipeline");

	filesSource = new FileSource(Environment.INSTANCE().projectDir);

	logger.trace("Created file source {}", filesSource);

	// the parser
	ILinkOneToOne<File, IStrategoParseOrAnalyzeResult> parserLink = new JSGLRLink();
	logger.trace("Created parser link {}", parserLink);

	// // link to map the parser over the files
	LinkMapperOneToOne<File, IStrategoParseOrAnalyzeResult> parserMapper = new LinkMapperOneToOne<File, IStrategoParseOrAnalyzeResult>(
		parserLink);
	filesSource.addSink(parserMapper);
	logger.trace("Created mapper {} for parser {}", parserMapper,
		parserLink);

	// link the analyzer to work on the files
	ILinkManyToMany<File, IStrategoParseOrAnalyzeResult> analyzerLink = new AnalyzerLink();
	filesSource.addSink(analyzerLink);
	logger.trace("Analyzer {} linked on file source {}", analyzerLink,
		filesSource);

	// create a Parser and Analyzer message extractor
	ILinkManyToMany<IStrategoParseOrAnalyzeResult, IMessage> messageSelector = new MessageExtractorLink();
	parserMapper.addSink(messageSelector);
	logger.trace("Message selector {} linked on parse mapper {}",
		messageSelector, parserMapper);
	analyzerLink.addSink(messageSelector);
	logger.trace("Message selector {} linked on analyzer {}",
		messageSelector, analyzerLink);

	messageSink = new MessageSink();
	messageSelector.addSink(messageSink);
	logger.trace("Message sink {} linked on message selector {}",
		messageSink, messageSelector);

	logger.info("Pipeline initialized");
    }

    // protected void unloadIndex() throws InterpreterException {
    // HybridInterpreter runtime = RuntimeService.INSTANCE().getRuntime(
    // LanguageService.INSTANCE().getAnyLanguage());
    // IOperatorRegistry idxLib = runtime.getContext().getOperatorRegistry(
    // "INDEX");
    // AbstractPrimitive unloadIdxPrim = idxLib.get("LANG_index_unload");
    // assert unloadIdxPrim.call(
    // runtime.getContext(),
    // new Strategy[0],
    // new IStrategoTerm[] { runtime.getFactory().makeString(
    // Environment.INSTANCE().projectDir.getAbsolutePath()) });
    // }

    public void run() {
	logger.debug("Beginning run");
	init();
	initPipeline();
	logger.trace("Beginning the push of changes");
	filesSource.poke();
	logger.trace("Emitting messages");
	emitMessages();
    }

}
