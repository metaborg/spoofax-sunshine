/**
 * 
 */
package org.spoofax.sunshine.framework.messages;

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
	private Throwable exception;
	private ARegion region;

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
		return str.toString();
	}
	
	
	public static Message newParseError(String file, IToken left, IToken right, String msg){
		final Message message = new Message();
		message.type = MessageType.PARSER_MESSAGE;
		message.severity = MessageSeverity.ERROR;
		message.file = file;
		message.region = new TokenRegion(left, right);
		message.msg = msg;
		return message;
	}
	
	public static Message newParseWarning(String file, IToken left, IToken right, String msg){
		final Message message = newParseError(file, left, right, msg);
		message.severity = MessageSeverity.WARNING;
		return message;
	}
	
	public static Message newParseErrorAtTop(String file, String msg){
		final Message message = new Message();
		message.type = MessageType.PARSER_MESSAGE;
		message.severity = MessageSeverity.ERROR;
		message.file = file;
		message.msg = msg;
		message.region = new PositionRegion(0, 0, 1, 0);
		return message;
	}
	
	public static Message newParseWarningAtTop(String file, String msg){
		final Message message = newParseErrorAtTop(file, msg);
		message.severity = MessageSeverity.WARNING;
		return message;
	}

}
