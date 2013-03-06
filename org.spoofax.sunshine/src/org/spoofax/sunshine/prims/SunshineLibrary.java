package org.spoofax.sunshine.prims;

import org.spoofax.interpreter.library.AbstractStrategoOperatorRegistry;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class SunshineLibrary extends AbstractStrategoOperatorRegistry {
	
	public static final String REGISTRY_NAME = "sunshine2spoofax";
	
	public SunshineLibrary() {
//		add(new NameDialogPrimitive());
//		add(new SubtermPrimitive());
//		add(new TermPathPrimitive());
		add(new ProjectPathPrimitive());
//		add(new PluginPathPrimitive());
//		add(new RefreshResourcePrimitive());
//		add(new QueueAnalysisPrimitive());
//		add(new QueueAnalysisCountPrimitive());
//		add(new QueueStrategyPrimitive());
//		add(new SetMarkersPrimitive());
//		add(new CandidateSortsPrimitive());
//		add(new GetAllProjectsPrimitive());
//		
//		add(new SetTotalWorkUnitsPrimitive());
//		add(new CompleteWorkUnitPrimitive());
//		add(new SaveAllResourcesPrimitive());
//		add(new MessageDialogPrimitive());
//		add(new LanguageDescriptionPrimitive());
//		add(new OverrideInputPrimitive());
//
//		add(new OriginSurroundingCommentsPrimitive());
//
//		add(new InSelectedFragmentPrimitive());
//
//		add(new OriginLanguagePrimitive());
	}

	public String getOperatorRegistryName() {
		return REGISTRY_NAME;
	}
}
