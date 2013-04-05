/**
 * 
 */
package org.spoofax.sunshine.services.analysis;

import java.io.File;
import java.util.Collection;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.sunshine.model.messages.IMessage;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public interface IAnalysisResult {

    IStrategoTerm ast();

    Collection<IMessage> messages();

    File file();
}
