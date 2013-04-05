/**
 * 
 */
package org.spoofax.sunshine.parser.model;

import java.io.File;
import java.util.Collection;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.sunshine.model.messages.IMessage;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 *
 */
public interface IParseOrAnalyzeResult<T> {
    public File file();

    public void setAst(IStrategoTerm ast);

    public IStrategoTerm ast();

    public void setMessages(Collection<IMessage> messages);

    public Collection<IMessage> messages();
}
