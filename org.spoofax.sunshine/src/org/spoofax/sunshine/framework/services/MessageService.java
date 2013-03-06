/**
 * 
 */
package org.spoofax.sunshine.framework.services;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.spoofax.sunshine.framework.messages.IMessage;
import org.spoofax.sunshine.framework.messages.MessageType;

/**
 * Singleton service for {@link IMessage} handling. This service provides registration of messages
 * as produced by other services. Messages can be contributed by services (
 * {@link #addMessage(IMessage)}), retrieved by other services ({@link #getMessages()}) and cleared
 * on request ({@link #clearMessages(String)}).
 * 
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

	/**
	 * Add an {@link IMessage} to recorded messages. Multiple equal messages (
	 * {@link IMessage#equals(Object)}) are only stored once.
	 * 
	 * @param msg
	 */
	public void addMessage(IMessage msg) {
		messages.add(msg);
	}
	
	/**
	 * Add all {@link IMessage} in the collection to the recorded messages.  Multiple equal messages (
	 * {@link IMessage#equals(Object)}) are only stored once.
	 * @param messages
	 */
	public void addMessage(Collection<IMessage> messages) {
		this.messages.addAll(messages);
	}

	/**
	 * Retrieve the recorded messages. The result contains no duplicate messages.
	 * 
	 * @return A set of {@link IMessage}.
	 */
	public Set<IMessage> getMessages() {
		return new HashSet<IMessage>(messages);
	}

	/**
	 * Delete all messages that are stored in this message registry.
	 */
	public void clearMessages() {
		messages.clear();
	}

	/**
	 * Delete all messages that have been registered for the given file. Messages not pertaining to
	 * the given file are preserved.
	 * 
	 * @param filename
	 *            A string representation of the {@link File} for which to clear messages.
	 */
	public void clearMessages(String filename) {
		for (MessageType ty : MessageType.values()) {
			clearMessages(filename, ty);
		}
	}

	/**
	 * Delete all messages of the given {@link MessageType} that have been registered for the given
	 * file. Messages not pertaining to the given file or of a different type are preserved.
	 * 
	 * @see #clearMessages(String)
	 * @param filename
	 *            A string representation of the {@link File} for which to clear messages.
	 * @param type
	 *            The type of messages that are to be cleared.
	 */
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
