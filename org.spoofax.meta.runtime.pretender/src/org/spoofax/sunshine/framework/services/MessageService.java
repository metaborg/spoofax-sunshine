/**
 * 
 */
package org.spoofax.sunshine.framework.services;

import java.util.HashSet;
import java.util.Set;

import org.spoofax.sunshine.framework.messages.IMessage;

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

}
