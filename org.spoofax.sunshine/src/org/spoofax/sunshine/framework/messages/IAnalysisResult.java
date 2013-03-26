/**
 * 
 */
package org.spoofax.sunshine.framework.messages;

import java.io.File;
import java.util.Collection;

import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 *
 */
public interface IAnalysisResult {
	IStrategoTerm getAst();
	Collection<IMessage> getMessages();
	Collection<IStrategoTerm> getRawMessages(MessageSeverity type);
	File getFile();
}
