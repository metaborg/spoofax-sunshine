/**
 * 
 */
package org.spoofax.sunshine.framework.messages;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class ResultApplAnalysisResult implements IAnalysisResult {

	private File file;
	private IStrategoTerm ast;
	private IStrategoList errors;
	private IStrategoList warnings;
	private IStrategoList notes;
	private final Collection<IMessage> messages = new ArrayList<IMessage>();

	public ResultApplAnalysisResult(IStrategoAppl resultTerm) {
		init(resultTerm);
	}

	private void init(IStrategoAppl resultTerm) {
		assert resultTerm != null;
		assert resultTerm.getSubtermCount() == 10;
		// partition, ast, errors, warnings, notes
		final String filename = ((IStrategoString) resultTerm.getSubterm(0)).stringValue();
		this.file = new File(filename);
		this.ast = resultTerm.getSubterm(2);
		this.errors = (IStrategoList) resultTerm.getSubterm(7);
		this.warnings = (IStrategoList) resultTerm.getSubterm(8);
		this.notes = (IStrategoList) resultTerm.getSubterm(9);
		messages.addAll(MessageHelper.makeMessages(this.file, MessageSeverity.ERROR, this.errors));
		messages.addAll(MessageHelper.makeMessages(this.file, MessageSeverity.WARNING, this.warnings));
		messages.addAll(MessageHelper.makeMessages(this.file, MessageSeverity.NOTE, this.notes));
	}

	@Override
	public IStrategoTerm getAst() {
		return ast;
	}

	@Override
	public Collection<IMessage> getMessages() {
		return messages;
	}

	@Override
	public File getFile() {
		return file;
	}

	@Override
	public Collection<IStrategoTerm> getRawMessages(MessageSeverity severity) {
		switch (severity) {
		case ERROR:
			return Arrays.asList(this.errors.getAllSubterms());
		case WARNING:
			return Arrays.asList(this.warnings.getAllSubterms());
		case NOTE:
			return Arrays.asList(this.notes.getAllSubterms());
		default:
			return null;
		}
	}

}
