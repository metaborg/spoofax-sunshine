package org.metaborg.sunshine.services.messages;

/**
 * 
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 */
public interface IMessage {
	MessageType type();

	MessageSeverity severity();

	String file();

	String message();

	CodeRegion region();

	Throwable getAttachedException();
}
