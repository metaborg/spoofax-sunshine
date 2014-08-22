/**
 * 
 */
package org.metaborg.sunshine.services.analyzer.legacy;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.vfs2.FileObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ILanguageService;
import org.metaborg.spoofax.core.resource.IResourceService;
import org.metaborg.spoofax.core.service.stratego.StrategoFacet;
import org.metaborg.sunshine.CompilerException;
import org.metaborg.sunshine.environment.LaunchConfiguration;
import org.metaborg.sunshine.environment.ServiceRegistry;
import org.metaborg.sunshine.model.messages.IMessage;
import org.metaborg.sunshine.model.messages.MessageHelper;
import org.metaborg.sunshine.model.messages.MessageSeverity;
import org.metaborg.sunshine.pipeline.connectors.ALinkOneToOne;
import org.metaborg.sunshine.pipeline.diff.Diff;
import org.metaborg.sunshine.services.RuntimeService;
import org.metaborg.sunshine.services.analyzer.AnalysisFileResult;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.HybridInterpreter;

import com.google.inject.Inject;

/**
 * @author vladvergu
 * 
 */
public class LegacyAnalyzerLink extends
		ALinkOneToOne<AnalysisFileResult, AnalysisFileResult> {

	private final static String ANALYSIS_CRASHED_MSG = "Analysis failed";

	private static final Logger logger = LogManager
			.getLogger(LegacyAnalyzerLink.class.getName());

	private final ILanguageService languageService;
	private final IResourceService resourceService;

	@Inject
	public LegacyAnalyzerLink(ILanguageService languageService,
			IResourceService resourceService) {
		this.languageService = languageService;
		this.resourceService = resourceService;
	}

	@Override
	public Diff<AnalysisFileResult> sinkWork(Diff<AnalysisFileResult> input) {
		return new Diff<AnalysisFileResult>(analyze(input.getPayload()),
				input.getDiffKind());
	}

	private AnalysisFileResult analyze(AnalysisFileResult parseResult) {
		logger.debug("Analyzing AST of file {}", parseResult.file());
		if (parseResult.ast() == null) {
			logger.info(
					"Analysis cannot continue because there is no AST for file {}",
					parseResult.file());
			return null;
		}
		ServiceRegistry serviceRegistry = ServiceRegistry.INSTANCE();
		final FileObject file = resourceService.resolve(parseResult.file());
		ILanguage lang = languageService
				.getByExt(file.getName().getExtension());

		LaunchConfiguration launch = serviceRegistry
				.getService(LaunchConfiguration.class);
		ITermFactory termFactory = launch.termFactory;
		HybridInterpreter runtime = serviceRegistry.getService(
				RuntimeService.class).getRuntime(lang);

		IStrategoString fileTerm = termFactory.makeString(launch.projectDir
				.toURI().relativize(parseResult.file().toURI()).toString());
		IStrategoString projectTerm = termFactory.makeString(launch.projectDir
				.getAbsolutePath());

		IStrategoTuple inputTerm = termFactory.makeTuple(parseResult.ast(),
				fileTerm, projectTerm);
		runtime.setCurrent(inputTerm);
		String function = lang.facet(StrategoFacet.class).analysisStrategy();
		boolean success;
		try {
			success = runtime.invoke(function);
			if (!success) {
				throw new CompilerException(ANALYSIS_CRASHED_MSG);
			} else {
				logger.debug("Ignoring further files to analyze. Not implemented");
				IStrategoTuple resultTuple = (IStrategoTuple) runtime.current();
				logger.trace("Analysis resulted in a {} tuple",
						resultTuple.getSubtermCount());
				return makeAnalysisResult(parseResult, resultTuple);
			}
		} catch (InterpreterException e) {
			throw new CompilerException(ANALYSIS_CRASHED_MSG, e);
		}
	}

	private AnalysisFileResult makeAnalysisResult(
			AnalysisFileResult parseResult, IStrategoTuple resultTuple) {
		assert resultTuple != null;
		assert resultTuple.getSubtermCount() == 5;
		IStrategoTerm ast = resultTuple.getSubterm(0);
		File file = parseResult.file();
		Collection<IMessage> messages = new HashSet<IMessage>();

		messages.addAll(MessageHelper.makeMessages(file, MessageSeverity.ERROR,
				(IStrategoList) resultTuple.getSubterm(1)));
		messages.addAll(MessageHelper.makeMessages(file,
				MessageSeverity.WARNING,
				(IStrategoList) resultTuple.getSubterm(2)));
		messages.addAll(MessageHelper.makeMessages(file, MessageSeverity.NOTE,
				(IStrategoList) resultTuple.getSubterm(3)));

		return new AnalysisFileResult(parseResult, file, messages, ast);
	}
}
