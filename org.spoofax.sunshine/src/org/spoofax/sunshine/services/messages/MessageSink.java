/**
 * 
 */
package org.spoofax.sunshine.services.messages;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.spoofax.sunshine.model.messages.IMessage;
import org.spoofax.sunshine.pipeline.ISinkMany;
import org.spoofax.sunshine.pipeline.diff.Diff;
import org.spoofax.sunshine.pipeline.diff.MultiDiff;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class MessageSink implements ISinkMany<IMessage> {

	private Set<IMessage> messages = new HashSet<IMessage>();

	@Override
	public void sink(MultiDiff<IMessage> product) {
		System.err.println("MessageSink sinking " + product.size() + " messages");
		final Iterator<Diff<IMessage>> diffIter = product.iterator();
		while (diffIter.hasNext()) {
			final Diff<IMessage> diff = diffIter.next();
			switch (diff.getDiffKind()) {
			case ADDITION:
				messages.add(diff.getPayload());
				break;
			case DELETION:
				messages.remove(diff.getPayload());
				break;
			default:
				throw new UnsupportedOperationException("MessageSink does not support DiffKind "
						+ diff.getDiffKind() + " for IMessage");
			}
		}
	}

	public Collection<IMessage> getMessages() {
		return new HashSet<IMessage>(messages);
	}

}
