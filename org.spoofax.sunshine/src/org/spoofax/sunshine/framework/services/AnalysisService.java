package org.spoofax.sunshine.framework.services;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.sunshine.analysis.DummyAnalysisController;
import org.spoofax.sunshine.analysis.IAnalysisController;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class AnalysisService {
	private static AnalysisService INSTANCE;

	private Map<File, IAnalysisController> controllers = new HashMap<File, IAnalysisController>();

	private AnalysisService() {
	}

	public static final AnalysisService INSTANCE() {
		if (INSTANCE == null) {
			INSTANCE = new AnalysisService();
		}
		return INSTANCE;
	}

	public IStrategoTerm analyze(File f) {
		return getAnalysisController(f).getAnalyzedAst();
	}

	private IAnalysisController getAnalysisController(File f) {
		IAnalysisController controller = controllers.get(f);
		if (controller == null) {
			controller = new DummyAnalysisController(f);
			controllers.put(f, controller);
		}
		return controller;
	}
}
