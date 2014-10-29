package org.metaborg.sunshine.model.messages;

import java.io.PrintStream;
import java.util.Collection;

import org.metaborg.spoofax.core.messages.IMessage;
import org.metaborg.spoofax.core.messages.MessageSeverity;
import org.metaborg.sunshine.services.messages.MessageSink;

public class MessageEmitter {

	private final MessageSink messager;

	public MessageEmitter(MessageSink messager) {
		this.messager = messager;
	}

	public void emitMessages(PrintStream os) {
		Collection<IMessage> messages = messager.getMessages();
		int i = 0;
		for (IMessage msg : messages) {
			os.println(i + ". " + msg);
			i++;
		}
	}

	public void emitSummary(PrintStream os) {
		Collection<IMessage> messages = messager.getMessages();
		MessageSeverity[] severities = MessageSeverity.values();
		int[] counts = new int[severities.length];
		int total = 0;
		for (IMessage msg : messages) {
			int severity = 0;
			for (; severity < severities.length; severity++) {
				if (severities[severity] == msg.severity())
					break;
			}
			counts[severity]++; // = counts[severity] + 1;
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
		for (IMessage message : messager.getMessages()) {
			if (message.severity() == severity) {
				return true;
			}
		}
		return false;
	}
}
