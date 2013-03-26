/**
 * 
 */
package org.spoofax.sunshine.framework.messages;

import java.io.PrintWriter;
import java.io.StringWriter;

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

}
