/**
 * 
 */
package org.spoofax.sunshine.framework.services;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.spoofax.sunshine.framework.messages.IMessage;
import org.spoofax.sunshine.framework.messages.MessageType;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class MessageService {
	private static MessageService INSTANCE;

	private final Set<IMessage> messages = new HashSet<IMessage>();

	private MessageService() {
	}

	public static final MessageService INSTANCE() {
		if (INSTANCE == null) {
			INSTANCE = new MessageService();
		}
		return INSTANCE;
	}

	public void addMessage(IMessage msg) {
		messages.add(msg);
	}

	public Set<IMessage> getMessages() {
		return new HashSet<IMessage>(messages);
	}

	public void clearAll() {
		messages.clear();
	}

	public void clearMessages(String filename) {
		clearMessages(filename, MessageType.PARSER_MESSAGE);
		clearMessages(filename, MessageType.ANALYSIS_MESSAGE);
	}

	public void clearMessages(String filename, MessageType type) {
		final Set<IMessage> toRemove = new HashSet<IMessage>();
		for (IMessage msg : messages) {
			if (msg.file().equals(filename) && msg.type() == type) {
				toRemove.add(msg);
			}
		}
		messages.removeAll(toRemove);
	}

}
