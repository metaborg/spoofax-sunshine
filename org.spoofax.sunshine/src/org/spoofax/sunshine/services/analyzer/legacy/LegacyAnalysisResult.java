package org.spoofax.sunshine.services.analyzer.legacy;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.sunshine.model.messages.IMessage;
import org.spoofax.sunshine.model.messages.MessageHelper;
import org.spoofax.sunshine.model.messages.MessageSeverity;
import org.spoofax.sunshine.parser.model.IStrategoParseOrAnalyzeResult;

public class LegacyAnalysisResult implements IStrategoParseOrAnalyzeResult {
	private File file;
	private IStrategoTerm ast;
	private final Collection<IMessage> messages = new LinkedList<IMessage>();
	private final IStrategoTerm previousAst;

	public LegacyAnalysisResult(File f, IStrategoTerm previousAst, IStrategoTuple resultTuple) {
		this.file = f;
		this.previousAst = previousAst;
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

	@Override
	public IStrategoTerm previousAst() {
		return previousAst;
	}
}