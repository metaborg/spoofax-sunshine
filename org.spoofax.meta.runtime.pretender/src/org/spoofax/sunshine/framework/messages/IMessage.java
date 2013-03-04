package org.spoofax.sunshine.framework.messages;

import org.spoofax.jsglr.client.imploder.IToken;

/**
 * 
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 */
public interface IMessage {
	MessageType type();
	MessageSeverity severity();
	String file();
	String message();
	IToken left();
	IToken right();
	Throwable getAttachedException();
}
