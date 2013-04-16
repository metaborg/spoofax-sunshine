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
import org.spoofax.sunshine.pipeline.connectors.LinkMapperOneToOne;
import org.spoofax.sunshine.services.LanguageService;
import org.spoofax.sunshine.services.analyzer.AnalyzerLink;
import org.spoofax.sunshine.services.filesource.FileSource;
import org.spoofax.sunshine.services.messages.MessageExtractorLink;
import org.spoofax.sunshine.services.messages.MessageSink;
import org.spoofax.sunshine.services.parser.JSGLRLink;
import org.spoofax.sunshine.services.pipelined.builders.BuilderInputTermFactoryLink;
import org.spoofax.sunshine.services.pipelined.builders.BuilderSink;

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
	LinkMapperOneToOne<File, IStrategoParseOrAnalyzeResult> parserMapper = new LinkMapperOneToOne<File, IStrategoParseOrAnalyzeResult>(
		new JSGLRLink());
	logger.trace("Created mapper {} for parser", parserMapper);
	ILinkManyToMany<File, IStrategoParseOrAnalyzeResult> analyzerLink = new AnalyzerLink();
	ILinkManyToMany<IStrategoParseOrAnalyzeResult, IMessage> messageSelector = new MessageExtractorLink();
	logger.trace("Message selector {} linked on parse mapper {}",
		messageSelector, parserMapper);
	messageSink = new MessageSink();

	logger.trace("Wiring pipeline up");
	filesSource.addSink(parserMapper);
	filesSource.addSink(analyzerLink);
	parserMapper.addSink(messageSelector);
	analyzerLink.addSink(messageSelector);
	messageSelector.addSink(messageSink);

	if (Environment.INSTANCE().getLaunchConfiguration().postAnalysisBuilder != null) {
	    logger.trace("Creating builder links for builder {}", Environment
		    .INSTANCE().getLaunchConfiguration().postAnalysisBuilder);
	    BuilderInputTermFactoryLink inputMakeLink = new BuilderInputTermFactoryLink(
		    new File(Environment.INSTANCE().projectDir, Environment
			    .INSTANCE().getLaunchConfiguration().builderTarget
			    .getPath()));
	    BuilderSink compileBuilder = new BuilderSink(Environment.INSTANCE()
		    .getLaunchConfiguration().postAnalysisBuilder);
	    logger.trace("Wiring builder up into pipeline");
	    analyzerLink.addSink(inputMakeLink);
	    inputMakeLink.addSink(compileBuilder);
	}
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
	// emitMessages();
    }

}
