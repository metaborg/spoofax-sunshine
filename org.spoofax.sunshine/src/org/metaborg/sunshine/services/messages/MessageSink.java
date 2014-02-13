/**
 * 
 */
package org.metaborg.sunshine.services.messages;

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metaborg.sunshine.environment.LaunchConfiguration;
import org.metaborg.sunshine.environment.ServiceRegistry;
import org.metaborg.sunshine.pipeline.ISinkMany;
import org.metaborg.sunshine.pipeline.diff.Diff;
import org.metaborg.sunshine.pipeline.diff.MultiDiff;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class MessageSink implements ISinkMany<IMessage> {
	private static final Logger logger = LogManager.getLogger(MessageSink.class
			.getName());

	@Override
	public void sink(MultiDiff<IMessage> product) {
		logger.info("Sinking {} messages", product.size());

		Set<IMessage> messages = new HashSet<IMessage>();

		ServiceRegistry services = ServiceRegistry.INSTANCE();
		boolean supresswarnings = services
				.getService(LaunchConfiguration.class).mainArguments.suppresswarnings;
		for (Diff<IMessage> msgDiff : product) {
			if (msgDiff.getPayload().severity() == MessageSeverity.ERROR
					|| !supresswarnings) {
				messages.add(msgDiff.getPayload());
			}
		}
		services.getService(MessageService.class).addMessages(messages);
	}

}
