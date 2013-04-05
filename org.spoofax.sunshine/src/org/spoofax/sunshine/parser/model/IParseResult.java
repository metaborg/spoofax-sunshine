/**
 * 
 */
package org.spoofax.sunshine.parser.model;

import java.io.File;
import java.util.Collection;

import org.spoofax.sunshine.model.messages.IMessage;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public interface IParseResult<T> {

    File file();

    void setAst(T ast);

    T ast();

    void setMessages(Collection<IMessage> messages);

    Collection<IMessage> messages();

}