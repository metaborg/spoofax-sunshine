package org.spoofax.sunshine.prims;

import org.spoofax.interpreter.library.AbstractStrategoOperatorRegistry;
import org.spoofax.sunshine.parser.prims.ParseFilePrimitive;

/**
 * 
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class SunshineLibrary extends AbstractStrategoOperatorRegistry {

	public static final String REGISTRY_NAME = "sunshine2spoofax";

	public SunshineLibrary() {
		add(new ProjectPathPrimitive());
		add(new SetTotalWorkUnitsPrimitive());
		add(new CompleteWorkUnitPrimitive());
		add(new ParseFilePrimitive());
	}

	public String getOperatorRegistryName() {
		return REGISTRY_NAME;
	}
}
