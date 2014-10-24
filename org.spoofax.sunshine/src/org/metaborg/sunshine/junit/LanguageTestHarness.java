package org.metaborg.sunshine.junit;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.vfs2.FileObject;
import org.junit.Before;
import org.metaborg.spoofax.core.language.ILanguageDiscoveryService;
import org.metaborg.spoofax.core.resource.IResourceService;
import org.metaborg.sunshine.environment.ServiceRegistry;
import org.metaborg.sunshine.environment.SunshineMainArguments;
import org.metaborg.sunshine.model.messages.IMessage;
import org.metaborg.sunshine.model.messages.MessageSeverity;
import org.metaborg.sunshine.services.analyzer.AnalysisFileResult;
import org.metaborg.sunshine.services.analyzer.AnalysisResult;
import org.metaborg.sunshine.services.analyzer.AnalysisService;
import org.metaborg.sunshine.services.parser.ParserService;

/**
 * Thin wrapper over Sunshine to use for testing languages' parser/analyzer
 * using JUnit
 * 
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public abstract class LanguageTestHarness {
	protected IResourceService resourceService;

	public abstract FileObject getPathToLanguageRepository();

	public abstract FileObject getPathToInputFile();

	@Before
	public void setUp() throws Exception {
		SunshineMainArguments args = new SunshineMainArguments();
		args.nonincremental = true;
		args.project = getPathToInputFile().getParent().getName().getPath();
		org.metaborg.sunshine.drivers.Main.initEnvironment(args);
		final ServiceRegistry services = ServiceRegistry.INSTANCE();
		resourceService = services.getService(IResourceService.class);

		final FileObject path = resourceService
				.resolve(getPathToLanguageRepository().getName().getPath());
		services.getService(ILanguageDiscoveryService.class).discover(path);
	}

	public void assertParseSucceeds(FileObject inputFile) {
		AnalysisFileResult parseResult = ServiceRegistry.INSTANCE()
				.getService(ParserService.class).parseFile(inputFile);
		assertNoMessage(parseResult, MessageSeverity.ERROR);
	}

	public void assertParseFails(FileObject inputFile) {
		try {
			assertParseSucceeds(inputFile);
		} catch (AssertionError ae) {
			return;
		}
		fail("Parse succeeded, failure expected");
	}

	public void assertAnalysisSucceeds(FileObject inputFile) {
		AnalysisFileResult parseResult = ServiceRegistry.INSTANCE()
				.getService(ParserService.class).parseFile(inputFile);
		Collection<AnalysisResult> analysisResults = ServiceRegistry
				.INSTANCE()
				.getService(AnalysisService.class)
				.analyze(
						Arrays.asList(new AnalysisFileResult[] { parseResult }));
		for (AnalysisResult result : analysisResults) {
			assertNotEquals("No analysis results", result.fileResults.size(), 0);
			assertNoMessage(result.fileResults, MessageSeverity.ERROR);
		}
	}

	public void assertAnalysisFails(FileObject inputFile) {
		try {
			assertAnalysisSucceeds(inputFile);
		} catch (AssertionError ae) {
			return;
		}
		fail("Analysis succeeded, failure expected");
	}

	public static void assertNoMessage(Collection<AnalysisFileResult> results,
			MessageSeverity severity) {
		for (AnalysisFileResult result : results) {
			for (IMessage msg : result.messages()) {
				assertNotEquals(severity + msg.toString() + "\n",
						msg.severity(), severity);
			}
		}
	}

	public static void assertNoMessage(AnalysisFileResult result,
			MessageSeverity severity) {
		for (IMessage msg : result.messages()) {
			assertNotEquals(msg.severity(), severity);
		}
	}

}
