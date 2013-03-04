/**
 * 
 */
package org.spoofax.sunshine.framework.messages;

import org.spoofax.jsglr.client.imploder.IToken;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class TokenMessage implements IMessage {

	public MessageType type;
	public MessageSeverity severity;
	public String file;
	public String msg;
	public IToken left;
	public IToken right;
	private Throwable exception;

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IMessage) {
			IMessage o = (IMessage) obj;
			boolean equal = true;
			equal &= file.equals(o.file());
			equal &= equal && left == null ? o.left() == null : left.compareTo(o.left()) == 0;
			equal &= right == null ? o.right() == null : right.compareTo(o.right()) == 0;
			equal &= type == o.type();
			equal &= severity == o.severity();
			equal &= msg.equals(o.message());
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
		hashBuilder.append(left != null ? left.hashCode() : "");
		hashBuilder.append(right != null ? right.hashCode() : "");
		return hashBuilder.toString().hashCode();
	}
	
	@Override
	public MessageType type() {
		return this.type;
	}

	@Override
	public MessageSeverity severity() {
		return this.severity;
	}

	@Override
	public String file() {
		return this.file;
	}

	@Override
	public String message() {
		return this.msg;
	}

	@Override
	public IToken left() {
		return this.left;
	}

	@Override
	public IToken right() {
		return this.right;
	}

	@Override
	public String toString() {
		// TODO proper toString
		return msg;
	}

	@Override
	public Throwable getAttachedException() {
		return this.exception;
	}

}
