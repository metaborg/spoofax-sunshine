/**
 * 
 */
package org.spoofax.sunshine.services.analyzer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.sunshine.model.messages.IMessage;
import org.spoofax.sunshine.model.messages.MessageHelper;
import org.spoofax.sunshine.model.messages.MessageSeverity;
import org.spoofax.sunshine.parser.model.IStrategoParseOrAnalyzeResult;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class ResultApplAnalysisResult implements IStrategoParseOrAnalyzeResult {

	private File file;
	private IStrategoTerm ast;
	private final Collection<IMessage> messages = new ArrayList<IMessage>();

	public ResultApplAnalysisResult(IStrategoAppl resultTerm) {
		init(resultTerm);
	}

	private void init(IStrategoAppl resultTerm) {
		assert resultTerm != null;
		assert resultTerm.getSubtermCount() == 7;
		final String filename = ((IStrategoString) resultTerm.getSubterm(0)).stringValue();
		this.file = new File(filename);
		this.ast = resultTerm.getSubterm(2);

		IStrategoList errors, warnings, notes;
		errors = (IStrategoList) resultTerm.getSubterm(4);
		warnings = (IStrategoList) resultTerm.getSubterm(5);
		notes = (IStrategoList) resultTerm.getSubterm(6);
		messages.addAll(MessageHelper.makeMessages(this.file, MessageSeverity.ERROR, errors));
		messages.addAll(MessageHelper.makeMessages(this.file, MessageSeverity.WARNING, warnings));
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
