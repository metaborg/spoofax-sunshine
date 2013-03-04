/**
 * 
 */
package org.spoofax.sunshine.messages;

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
	public IToken left;
	public IToken right;

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

}
