/**
 * 
 */
package org.spoofax.sunshine.messages;

import java.util.Collection;

/**
 * @author vladvergu
 *
 */
public interface IMessageProducer {
	Collection<IMessage> getMessages();
}
