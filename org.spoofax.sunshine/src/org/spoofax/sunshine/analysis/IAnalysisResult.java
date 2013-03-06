/**
 * 
 */
package org.spoofax.sunshine.analysis;

import java.util.Set;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.sunshine.framework.messages.IMessage;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 *
 */
public interface IAnalysisResult {

	IStrategoTerm getAnalyzedAst();
	Set<IMessage> getErrors();
	Set<IMessage> getWarnings();
	Set<IMessage> getNotes();
}
