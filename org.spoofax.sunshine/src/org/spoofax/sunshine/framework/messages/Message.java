/**
 * 
 */
package org.spoofax.sunshine.framework.messages;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.spoofax.jsglr.client.imploder.IToken;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class Message implements IMessage {

	public MessageType type;
	public MessageSeverity severity;
	public String file;
	public String msg;
	public Throwable exception;
	public ARegion region;

	@Override
	public MessageType type() {
		return type;
	}

	@Override
	public MessageSeverity severity() {
		return severity;
	}

	@Override
	public String file() {
		return file;
	}

	@Override
	public String message() {
		return msg;
	}

	@Override
	public Throwable getAttachedException() {
		return exception;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IMessage) {
			IMessage o = (IMessage) obj;
			boolean equal = true;
			equal &= file.equals(o.file());
			equal &= type == o.type();
			equal &= severity == o.severity();
			equal &= msg.equals(o.message());
			equal &= region.equals(o.region());
			return equal;
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		final StringBuilder hashBuilder = new StringBuilder();
		hashBuilder.append(type.hashCode());
		hashBuilder.append(severity.hashCode());
		hashBuilder.append(file);
		hashBuilder.append(msg);
		hashBuilder.append(region.hashCode());
		return hashBuilder.toString().hashCode();
	}

	@Override
	public ARegion region() {
		return region;
	}

	@Override
	public String toString() {
		final StringBuilder str = new StringBuilder();
		str.append(severity);
		str.append(" ");
		str.append(file);
		str.append("@");
		str.append(region.toString());
		str.append(" ");
		str.append("\n\t");
		str.append(msg);
		str.append("\n");
		if (exception != null) {
			str.append("\tCaused by:\n");
			final StringWriter sw = new StringWriter();
			final PrintWriter pw = new PrintWriter(sw);
			exception.printStackTrace(pw);
			str.append(sw.toString());
		}
		return str.toString();
	}

	public static Message newMessage(String file, IToken left, IToken right, String msg, MessageSeverity severity,
			MessageType type) {
		final Message message = new Message();
		message.type = type;
		message.severity = severity;
		message.file = file;
		message.region = new TokenRegion(left, right);
		message.msg = msg;
		return message;
	}

	public static Message newParseMessage(String file, IToken left, IToken right, String msg, MessageSeverity severity) {
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
		return newMessage(file, left, right, msg, MessageSeverity.ERROR, MessageType.ANALYSIS_MESSAGE);
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

	private static Message newAtTop(String file, String msg, MessageType type, MessageSeverity severity) {
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

}
