/**
 * 
 */
package org.metaborg.sunshine.services.messages;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metaborg.sunshine.Environment;
import org.metaborg.sunshine.model.messages.IMessage;
import org.metaborg.sunshine.model.messages.MessageSeverity;
import org.metaborg.sunshine.pipeline.ISinkMany;
import org.metaborg.sunshine.pipeline.diff.Diff;
import org.metaborg.sunshine.pipeline.diff.MultiDiff;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class MessageSink implements ISinkMany<IMessage> {
	private static final Logger logger = LogManager.getLogger(MessageSink.class.getName());
	private Set<IMessage> messages = new HashSet<IMessage>();

	@Override
	public void sink(MultiDiff<IMessage> product) {
		logger.info("Sinking {} messages", product.size());

		for (Diff<IMessage> msgDiff : product) {
			if (msgDiff.getPayload().severity() == MessageSeverity.ERROR
					|| !Environment.INSTANCE().getMainArguments().suppresswarnings) {
				messages.add(msgDiff.getPayload());
			}
		}
	}

	public Collection<IMessage> getMessages() {
		return new HashSet<IMessage>(messages);
	}

}
