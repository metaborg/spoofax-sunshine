/**
 * 
 */
package org.metaborg.sunshine.services;

import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.sunshine.environment.ServiceRegistry;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * A directly invokable reference to a Stratego strategy in a {@link ALanguage}.
 * In effect instances
 * of this class are closures over the callable strategy and the language.
 * 
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class StrategoCallTarget {

	private final ILanguage language;
	private final String strategyName;

	public StrategoCallTarget(ILanguage language, String strategyName) {
		this.language = language;
		this.strategyName = strategyName;
	}

	public ILanguage getLanguage() {
		return language;
	}

	public String getStrategyName() {
		return strategyName;
	}

	public IStrategoTerm invoke(IStrategoTerm input) {
		return ServiceRegistry.INSTANCE().getService(StrategoCallService.class)
				.callStratego(language, strategyName, input);
	}
}
