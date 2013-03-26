/**
 * 
 */
package org.spoofax.sunshine.framework.messages;

import static org.spoofax.interpreter.core.Tools.isTermList;
import static org.spoofax.interpreter.core.Tools.isTermTuple;
import static org.spoofax.interpreter.core.Tools.termAt;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class TupleBasedAnalysisResult implements IAnalysisResult {

	private final IStrategoTerm ast;
	private final Collection<IMessage> msgs;
	private final File file;

	public TupleBasedAnalysisResult(File file, IStrategoTerm tup) {
		assert file != null;
		assert isTermTuple(tup);
		assert tup.getSubtermCount() == 4;
		assert isTermList(termAt(tup, 1));
		assert isTermList(termAt(tup, 2));
		assert isTermList(termAt(tup, 3));

		this.file = file;
		ast = tup.getSubterm(0);
		msgs = new LinkedList<IMessage>();

		final IStrategoList errors = termAt(tup, 1);
		msgs.addAll(MessageHelper.makeMessages(file, MessageSeverity.ERROR, errors));

		final IStrategoList warnings = termAt(tup, 1);
		msgs.addAll(MessageHelper.makeMessages(file, MessageSeverity.WARNING, warnings));

		final IStrategoList notes = termAt(tup, 1);
		msgs.addAll(MessageHelper.makeMessages(file, MessageSeverity.NOTE, notes));
	}

	@Override
	public IStrategoTerm getAst() {
		return ast;
	}

	@Override
	public Collection<IMessage> getMessages() {
		return msgs;
	}

	@Override
	public File getFile() {
		return file;
	}

	@Override
	public Collection<IStrategoTerm> getRawMessages(MessageSeverity type) {
		throw new RuntimeException("Not implemented");
	}

}
