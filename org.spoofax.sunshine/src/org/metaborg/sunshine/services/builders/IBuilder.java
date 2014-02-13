/**
 * 
 */
package org.metaborg.sunshine.services.builders;

import org.metaborg.sunshine.services.language.ALanguage;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * An interface to a language builder
 * 
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public interface IBuilder {

	/**
	 * The name of the builder as specified in the Entity-Menus.esv under the
	 * <code>action: "name"</code> field
	 * 
	 * @return
	 */
	public String getName();

	/**
	 * Indicates whether the builder should be applied to the source AST instead of the analyzed
	 * AST.
	 * 
	 * @return
	 */
	public boolean isOnSource();

	/**
	 * Indicates whether the builder is annotated with (meta) or not
	 * 
	 * @return
	 */
	public boolean isMeta();

	/**
	 * @return the name of strategy that this builder invokes
	 */
	public String getInvocationTarget();

	/**
	 * 
	 * @return the {@link ALanguage} that defines this builder
	 */
	public ALanguage getLanguage();

	/**
	 * Invokes this builder on the given input returning the result.
	 * 
	 * @param input
	 * @return
	 */
	public IStrategoTerm invoke(IStrategoTerm input);

}
