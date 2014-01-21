/**
 * 
 */
package org.metaborg.sunshine.services;

import org.metaborg.sunshine.environment.ServiceRegistry;
import org.metaborg.sunshine.services.language.ALanguage;
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

	private final ALanguage language;
	private final String strategyName;

	public StrategoCallTarget(ALanguage language, String strategyName) {
		this.language = language;
		this.strategyName = strategyName;
	}

	public ALanguage getLanguage() {
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
