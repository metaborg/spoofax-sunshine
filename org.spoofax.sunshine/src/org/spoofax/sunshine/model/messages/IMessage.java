package org.spoofax.sunshine.model.messages;


/**
 * 
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 */
public interface IMessage {
	MessageType type();
	MessageSeverity severity();
	String file();
	String message();
	ARegion region();
	Throwable getAttachedException();
}
