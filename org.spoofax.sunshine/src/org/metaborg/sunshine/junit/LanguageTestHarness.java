package org.metaborg.sunshine.junit;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.metaborg.sunshine.Environment;
import org.metaborg.sunshine.drivers.SunshineMainArguments;
import org.metaborg.sunshine.model.messages.IMessage;
import org.metaborg.sunshine.model.messages.MessageSeverity;
import org.metaborg.sunshine.services.analyzer.AnalysisResult;
import org.metaborg.sunshine.services.analyzer.AnalysisService;
import org.metaborg.sunshine.services.language.LanguageDiscoveryService;
import org.metaborg.sunshine.services.parser.ParserService;

/**
 * Thin wrapper over Sunshine to use for testing languages' parser/analyzer using JUnit
 * 
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public abstract class LanguageTestHarness {

	public abstract Path getPathToLanguageRepository();

	public abstract Path getPathToInputFile();

	@Before
	public void setUp() throws Exception {
		SunshineMainArguments args = new SunshineMainArguments();
		args.nonincremental = true;
		args.project = getPathToInputFile().getParent().toAbsolutePath().toString();
		Environment env = Environment.INSTANCE();
		env.setMainArguments(args);
		env.setProjectDir(new File(args.project));
		LanguageDiscoveryService.INSTANCE().discover(getPathToLanguageRepository());
	}

	public void assertParseSucceeds(File inputFile) {
		AnalysisResult parseResult = ParserService.INSTANCE().parseFile(inputFile);
		assertNoMessage(parseResult, MessageSeverity.ERROR);
	}

	public void assertParseFails(File inputFile) {
		try {
			assertParseSucceeds(inputFile);
		} catch (AssertionError ae) {
			return;
		}
		fail("Parse succeeded, failure expected");
	}

	public void assertAnalysisSucceeds(File inputFile) {
		Collection<AnalysisResult> analysisResult = AnalysisService.INSTANCE().analyze(
				Arrays.asList(new File[] { inputFile }));
		assertNotEquals("No analysis results", analysisResult.size(), 0);
		assertNoMessage(analysisResult, MessageSeverity.ERROR);
	}

	public void assertAnalysisFails(File inputFile) {
		try {
			assertAnalysisSucceeds(inputFile);
		} catch (AssertionError ae) {
			return;
		}
		fail("Analysis succeeded, failure expected");
	}

	public static void assertNoMessage(Collection<AnalysisResult> results, MessageSeverity severity) {
		for (AnalysisResult result : results) {
			for (IMessage msg : result.messages()) {
				assertNotEquals(severity + msg.toString() + "\n", msg.severity(), severity);
			}
		}
	}

	public static void assertNoMessage(AnalysisResult result, MessageSeverity severity) {
		for (IMessage msg : result.messages()) {
			assertNotEquals(msg.severity(), severity);
		}
	}

}
