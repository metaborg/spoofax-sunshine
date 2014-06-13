/**
 * 
 */
package org.metaborg.sunshine.services.analyzer;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;

import org.metaborg.sunshine.model.messages.IMessage;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * @author vladvergu
 * 
 */
public class AnalysisFileResult {

	private final Collection<IMessage> messages = new LinkedList<IMessage>();
	private File file;
	private AnalysisFileResult previous;
	private IStrategoTerm ast;

	public AnalysisFileResult(AnalysisFileResult previous, File f, Collection<IMessage> messages,
			IStrategoTerm ast) {
		this.previous = previous;
		this.file = f;
		this.ast = ast;
		this.messages.addAll(messages);
	}

	public Collection<IMessage> messages() {
		return this.messages;
	}

	public IStrategoTerm ast() {
		return ast;
	}

	public File file() {
		return file;
	}

	public AnalysisFileResult previousResult() {
		return previous;
	}
}
