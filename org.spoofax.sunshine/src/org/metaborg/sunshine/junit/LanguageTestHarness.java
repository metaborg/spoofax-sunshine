package org.metaborg.sunshine.junit;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collection;

import org.apache.commons.vfs2.FileObject;
import org.junit.Before;
import org.metaborg.spoofax.core.analysis.AnalysisFileResult;
import org.metaborg.spoofax.core.analysis.AnalysisResult;
import org.metaborg.spoofax.core.analysis.AnalysisService;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ILanguageDiscoveryService;
import org.metaborg.spoofax.core.language.ILanguageIdentifierService;
import org.metaborg.spoofax.core.messages.IMessage;
import org.metaborg.spoofax.core.messages.MessageSeverity;
import org.metaborg.spoofax.core.parser.IParseService;
import org.metaborg.spoofax.core.parser.ParseResult;
import org.metaborg.spoofax.core.resource.IResourceService;
import org.metaborg.sunshine.environment.ServiceRegistry;
import org.metaborg.sunshine.environment.SunshineMainArguments;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.common.collect.Lists;

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

	public void assertParseSucceeds(FileObject inputFile) throws IOException {
		final ServiceRegistry serviceRegistry = ServiceRegistry.INSTANCE();
		final ILanguage language = serviceRegistry.getService(
				ILanguageIdentifierService.class).identify(inputFile);
		@SuppressWarnings("unchecked")
		final ParseResult<IStrategoTerm> parseResult = serviceRegistry
				.getService(IParseService.class).parse(inputFile, language);
		assertNoMessage(parseResult, MessageSeverity.ERROR);
	}

	public void assertParseFails(FileObject inputFile) {
		try {
			assertParseSucceeds(inputFile);
		} catch (AssertionError | IOException e) {
			return;
		}
		fail("Parse succeeded, failure expected");
	}

	public void assertAnalysisSucceeds(FileObject inputFile) throws IOException {
		final ServiceRegistry serviceRegistry = ServiceRegistry.INSTANCE();
		final ILanguage language = serviceRegistry.getService(
				ILanguageIdentifierService.class).identify(inputFile);
		@SuppressWarnings("unchecked")
		final ParseResult<IStrategoTerm> parseResult = serviceRegistry
				.getService(IParseService.class).parse(inputFile, language);
		@SuppressWarnings("unchecked")
		Collection<AnalysisResult> analysisResults = ServiceRegistry.INSTANCE()
				.getService(AnalysisService.class)
				.analyze(Lists.newArrayList(parseResult));
		for (AnalysisResult result : analysisResults) {
			assertNotEquals("No analysis results", result.fileResults.size(), 0);
			assertNoMessage(result.fileResults, MessageSeverity.ERROR);
		}
	}

	public void assertAnalysisFails(FileObject inputFile) throws IOException {
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

	public static void assertNoMessage(ParseResult<IStrategoTerm> result,
			MessageSeverity severity) {
		for (IMessage msg : result.messages) {
			assertNotEquals(msg.severity(), severity);
		}
	}
}
