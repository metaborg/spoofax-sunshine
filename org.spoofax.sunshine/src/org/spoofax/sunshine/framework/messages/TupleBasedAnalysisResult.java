/**
 * 
 */
package org.spoofax.sunshine.framework.messages;

import static org.spoofax.interpreter.core.Tools.isTermList;
import static org.spoofax.interpreter.core.Tools.isTermTuple;
import static org.spoofax.interpreter.core.Tools.termAt;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getLeftToken;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getRightToken;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.hasImploderOrigin;
import static org.spoofax.terms.attachments.OriginAttachment.tryGetOrigin;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import org.spoofax.interpreter.terms.ISimpleTerm;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.sunshine.framework.services.RuntimeService;
import org.strategoxt.imp.generator.postprocess_feedback_results_0_0;
import org.strategoxt.imp.generator.sdf2imp;
import org.strategoxt.lang.Context;

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
		msgs.addAll(makeMessages(file, MessageSeverity.ERROR, errors));

		final IStrategoList warnings = termAt(tup, 1);
		msgs.addAll(makeMessages(file, MessageSeverity.WARNING, warnings));

		final IStrategoList notes = termAt(tup, 1);
		msgs.addAll(makeMessages(file, MessageSeverity.NOTE, notes));
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

	private static Collection<IMessage> makeMessages(File file, MessageSeverity severity, IStrategoList msgs) {
		final Collection<IMessage> result = new ArrayList<IMessage>(msgs.getSubtermCount());

		final Context context = RuntimeService.INSTANCE().getRuntime(file).getCompiledContext();
		// final Context context = runtime.getCompiledContext();
		sdf2imp.init(context);
		final IStrategoList processedMsgs = (IStrategoList) postprocess_feedback_results_0_0.instance.invoke(context,
				msgs);

		for (IStrategoTerm msg : processedMsgs.getAllSubterms()) {
			IStrategoTerm term;
			String message;
			if (isTermTuple(msg) && msg.getSubtermCount() == 2) {
				term = termAt(msg, 0);
				IStrategoString messageTerm = termAt(msg, 1);
				message = messageTerm.stringValue();
			} else {
				term = msg;
				message = msg.toString() + " (no tree node indicated)";
			}

			final ISimpleTerm node = minimizeMarkerSize(getClosestAstNode(term));
			final IToken left = getLeftToken(node);
			final IToken right = getRightToken(node);
			
			final Message resMsg = Message.newAnalysisMessage(file.getPath(), left, right, message, severity);
			result.add(resMsg);
		}

		return result;
	}

	/**
	 * Given an stratego term, give the first AST node associated with any of its subterms, doing a
	 * depth-first search.
	 */
	private static ISimpleTerm getClosestAstNode(IStrategoTerm term) {
		if (hasImploderOrigin(term)) {
			return tryGetOrigin(term);
		} else if (term == null) {
			return null;
		} else {
			for (int i = 0; i < term.getSubtermCount(); i++) {
				ISimpleTerm result = getClosestAstNode(termAt(term, i));
				if (result != null)
					return result;
			}
			return null;
		}
	}

	private static ISimpleTerm minimizeMarkerSize(ISimpleTerm node) {
		// TODO: prefer lexical nodes when minimizing marker size? (e.g., not 'private')
		if (node == null)
			return null;
		while (getLeftToken(node).getLine() < getRightToken(node).getLine()) {
			if (node.getSubtermCount() == 0)
				break;
			node = node.getSubterm(0);
		}
		return node;
	}

}
