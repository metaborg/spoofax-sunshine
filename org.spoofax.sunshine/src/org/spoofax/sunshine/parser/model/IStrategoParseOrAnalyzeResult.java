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
public interface IStrategoParseOrAnalyzeResult extends
	IParseOrAnalyzeResult<IStrategoTerm> {

    public IStrategoTerm ast();

    public Collection<IMessage> messages();

    public File file();
}