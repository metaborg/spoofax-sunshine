package org.spoofax.sunshine.model.messages;

import java.io.PrintStream;
import java.util.Collection;

import org.spoofax.sunshine.services.messages.MessageSink;

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
}
