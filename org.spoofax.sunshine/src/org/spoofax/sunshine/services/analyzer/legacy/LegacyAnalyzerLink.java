/**
 * 
 */
package org.spoofax.sunshine.services.analyzer.legacy;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.sunshine.CompilerException;
import org.spoofax.sunshine.Environment;
import org.spoofax.sunshine.model.language.ALanguage;
import org.spoofax.sunshine.model.messages.IMessage;
import org.spoofax.sunshine.model.messages.MessageHelper;
import org.spoofax.sunshine.model.messages.MessageSeverity;
import org.spoofax.sunshine.parser.model.IStrategoParseOrAnalyzeResult;
import org.spoofax.sunshine.pipeline.connectors.ALinkOneToOne;
import org.spoofax.sunshine.pipeline.diff.Diff;
import org.spoofax.sunshine.services.LanguageService;
import org.spoofax.sunshine.services.RuntimeService;
import org.spoofax.sunshine.services.parser.SourceAttachment;
import org.strategoxt.HybridInterpreter;

/**
 * @author vladvergu
 * 
 */
public class LegacyAnalyzerLink extends ALinkOneToOne<IStrategoTerm, IStrategoParseOrAnalyzeResult> {

	private final static String ANALYSIS_CRASHED_MSG = "Analysis failed";

	private static final Logger logger = LogManager.getLogger(LegacyAnalyzerLink.class.getName());

	@Override
	public Diff<IStrategoParseOrAnalyzeResult> sinkWork(Diff<IStrategoTerm> input) {
		IStrategoTerm ast = input.getPayload();
		return new Diff<IStrategoParseOrAnalyzeResult>(analyze(SourceAttachment.getResource(ast),
				ast), input.getDiffKind());
	}

	private IStrategoParseOrAnalyzeResult analyze(File file, IStrategoTerm ast) {
		logger.debug("Analyzing AST of file {}", file);
		ALanguage lang = LanguageService.INSTANCE().getLanguageByExten(file);

		ITermFactory termFactory = Environment.INSTANCE().termFactory;
		HybridInterpreter runtime = RuntimeService.INSTANCE().getRuntime(lang);

		IStrategoString fileTerm = termFactory.makeString(Environment.INSTANCE().projectDir.toURI()
				.relativize(file.toURI()).toString());
		IStrategoString projectTerm = termFactory.makeString(Environment.INSTANCE().projectDir
				.getAbsolutePath());

		IStrategoTuple inputTerm = termFactory.makeTuple(ast, fileTerm, projectTerm);
		runtime.setCurrent(inputTerm);
		String function = lang.getAnalysisFunction();
		boolean success;
		try {
			success = runtime.invoke(function);
			if (!success) {
				throw new CompilerException(ANALYSIS_CRASHED_MSG);
			} else {
				logger.warn("Ignoring further files to analyze. Not implemented");
				IStrategoTuple resultTuple = (IStrategoTuple) runtime.current();
				logger.trace("Analysis resulted in a {} tuple", resultTuple.getSubtermCount());
				return new LegacyAnalysisResult(file, resultTuple);
			}
		} catch (InterpreterException e) {
			throw new CompilerException(ANALYSIS_CRASHED_MSG, e);
		}
	}

	private class LegacyAnalysisResult implements IStrategoParseOrAnalyzeResult {
		private File file;
		private IStrategoTerm ast;
		private final Collection<IMessage> messages = new LinkedList<IMessage>();

		public LegacyAnalysisResult(File f, IStrategoTuple resultTuple) {
			this.file = f;
			init(resultTuple);

		}

		private void init(IStrategoTuple resultTuple) {
			assert resultTuple != null;
			assert resultTuple.getSubtermCount() == 5;
			this.ast = resultTuple.getSubterm(0);
			IStrategoList errors, warnings, notes;
			errors = (IStrategoList) resultTuple.getSubterm(1);
			warnings = (IStrategoList) resultTuple.getSubterm(2);
			notes = (IStrategoList) resultTuple.getSubterm(3);
			messages.addAll(MessageHelper.makeMessages(this.file, MessageSeverity.ERROR, errors));
			messages.addAll(MessageHelper
					.makeMessages(this.file, MessageSeverity.WARNING, warnings));
			messages.addAll(MessageHelper.makeMessages(this.file, MessageSeverity.NOTE, notes));
		}

		@Override
		public IStrategoTerm ast() {
			return ast;
		}

		@Override
		public Collection<IMessage> messages() {
			return messages;
		}

		@Override
		public File file() {
			return file;
		}

		@Override
		public void setAst(IStrategoTerm ast) {
			throw new UnsupportedOperationException("Cannot explicitly set AST");
		}

		@Override
		public void setMessages(Collection<IMessage> messages) {
			throw new UnsupportedOperationException("Cannot explicitly set messages");
		}
	}
}
