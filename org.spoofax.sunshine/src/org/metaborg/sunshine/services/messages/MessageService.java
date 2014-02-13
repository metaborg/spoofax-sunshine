/**
 * 
 */
package org.metaborg.sunshine.services.messages;

import java.io.PrintStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Service to maintain and emit messages submitted from various places.
 * 
 * @author vladvergu
 * 
 */
public class MessageService {
	private static final Logger logger = LogManager
			.getLogger(MessageService.class.getName());

	private final List<IMessage> messages = new LinkedList<>();

	public void addMessages(Collection<IMessage> newMessages) {
		logger.debug("Registering {} new messages", newMessages.size());
		messages.addAll(newMessages);
	}

	public void clear() {
		logger.debug("Clearing all {} messages", messages.size());
		messages.clear();
	}

	public void emitMessages(PrintStream os) {
		int i = 0;
		for (IMessage msg : messages) {
			os.println(i + ". " + msg);
			i++;
		}
	}

	public void emitSummary(PrintStream os) {
		MessageSeverity[] severities = MessageSeverity.values();
		int[] counts = new int[severities.length];
		int total = 0;
		for (IMessage msg : messages) {
			int severity = 0;
			for (; severity < severities.length; severity++) {
				if (severities[severity] == msg.severity())
					break;
			}
			counts[severity]++;
			total++;
		}
		os.print(total + " messages (");
		for (int severity = 0; severity < severities.length; severity++) {
			os.print(counts[severity] + " ");
			os.print(severities[severity]);
			if (severity < severities.length - 1) {
				os.print(", ");
			}
		}
		os.println(")");
	}

	public boolean hasErrors() {
		return hasMessage(MessageSeverity.ERROR);
	}

	public boolean hasWarnings() {
		return hasMessage(MessageSeverity.WARNING);
	}

	public boolean hasNotes() {
		return hasMessage(MessageSeverity.NOTE);
	}

	private boolean hasMessage(MessageSeverity severity) {
		for (IMessage message : messages) {
			if (message.severity() == severity) {
				return true;
			}
		}
		return false;
	}
}
