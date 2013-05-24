/**
 * 
 */
package org.spoofax.sunshine.model.messages;

import static org.spoofax.interpreter.core.Tools.isTermTuple;
import static org.spoofax.interpreter.core.Tools.termAt;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getLeftToken;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getRightToken;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.hasImploderOrigin;
import static org.spoofax.terms.attachments.OriginAttachment.tryGetOrigin;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.spoofax.interpreter.terms.ISimpleTerm;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.sunshine.services.RuntimeService;
import org.strategoxt.imp.generator.postprocess_feedback_results_0_0;
import org.strategoxt.imp.generator.sdf2imp;
import org.strategoxt.lang.Context;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class MessageHelper {
	public static Collection<IMessage> makeMessages(File file, MessageSeverity severity,
			IStrategoList msgs) {
		final Collection<IMessage> result = new ArrayList<IMessage>(msgs.getSubtermCount());

		final Context context = RuntimeService.INSTANCE().getRuntime(file).getCompiledContext();
		// final Context context = runtime.getCompiledContext();
		sdf2imp.init(context);
		final IStrategoList processedMsgs = (IStrategoList) postprocess_feedback_results_0_0.instance
				.invoke(context, msgs);

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

			final Message resMsg = newAnalysisMessage(file.getPath(), left, right, message,
					severity);
			result.add(resMsg);
		}

		return result;
	}

	public static Message newMessage(String file, IToken left, IToken right, String msg,
			MessageSeverity severity, MessageType type) {
		final Message message = new Message();
		message.type = type;
		message.severity = severity;
		message.file = file;
		message.region = new TokenRegion(left, right);
		message.msg = msg;
		return message;
	}

	public static Message newParseMessage(String file, IToken left, IToken right, String msg,
			MessageSeverity severity) {
		return newMessage(file, left, right, msg, MessageSeverity.ERROR, MessageType.PARSER_MESSAGE);
	}

	public static Message newParseError(String file, IToken left, IToken right, String msg) {
		return newParseMessage(file, left, right, msg, MessageSeverity.ERROR);
	}

	public static Message newParseWarning(String file, IToken left, IToken right, String msg) {
		return newParseMessage(file, left, right, msg, MessageSeverity.WARNING);
	}

	public static Message newAnalysisMessage(String file, IToken left, IToken right, String msg,
			MessageSeverity severity) {
		return newMessage(file, left, right, msg, MessageSeverity.ERROR,
				MessageType.ANALYSIS_MESSAGE);
	}

	public static Message newAnalysisError(String file, IToken left, IToken right, String msg) {
		return newAnalysisMessage(file, left, right, msg, MessageSeverity.ERROR);
	}

	public static Message newAnalysisWarning(String file, IToken left, IToken right, String msg) {
		return newAnalysisMessage(file, left, right, msg, MessageSeverity.WARNING);
	}

	public static Message newAnalysisNote(String file, IToken left, IToken right, String msg) {
		return newAnalysisMessage(file, left, right, msg, MessageSeverity.NOTE);
	}

	private static Message newAtTop(String file, String msg, MessageType type,
			MessageSeverity severity) {
		final Message message = new Message();
		message.type = type;
		message.severity = severity;
		message.file = file;
		message.msg = msg;
		message.region = new PositionRegion(0, 0, 1, 0);
		return message;
	}

	private static Message newErrorAtTop(String file, String msg, MessageType type) {
		return newAtTop(file, msg, type, MessageSeverity.ERROR);
	}

	private static Message newWarningAtTop(String file, String msg, MessageType type) {
		return newAtTop(file, msg, type, MessageSeverity.WARNING);
	}

	public static Message newParseErrorAtTop(String file, String msg) {
		return newErrorAtTop(file, msg, MessageType.PARSER_MESSAGE);
	}

	public static Message newParseWarningAtTop(String file, String msg) {
		return newWarningAtTop(file, msg, MessageType.PARSER_MESSAGE);
	}

	public static Message newAnalysisErrorAtTop(String file, String msg) {
		return newErrorAtTop(file, msg, MessageType.ANALYSIS_MESSAGE);
	}

	public static Message newAnalysisWarningAtTop(String file, String msg) {
		return newWarningAtTop(file, msg, MessageType.ANALYSIS_MESSAGE);
	}

	public static Message newBuilderErrorAtTop(String file, String msg) {
		return newErrorAtTop(file, msg, MessageType.BUILDER_MESSAGE);
	}

	public static Message newBuilderWarningAtTop(String file, String msg) {
		return newWarningAtTop(file, msg, MessageType.BUILDER_MESSAGE);
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
		// TODO: prefer lexical nodes when minimizing marker size? (e.g., not
		// 'private')
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
