/**
 * 
 */
package org.spoofax.sunshine.analysis;

import java.io.File;

import org.spoofax.interpreter.library.LoggingIOAgent;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.sunshine.framework.language.ALanguage;
import org.spoofax.sunshine.framework.messages.Message;
import org.spoofax.sunshine.framework.services.LanguageService;
import org.spoofax.sunshine.framework.services.MessageService;
import org.spoofax.sunshine.framework.services.ParseService;
import org.spoofax.sunshine.framework.services.RuntimeService;
import org.spoofax.sunshine.terms.NewInputTermBuilder;
import org.strategoxt.HybridInterpreter;

/**
 * An analysis controller that analyzes single files and caches results. Cached analyzed ASTs never
 * expire.
 * 
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class SingleFileCachingAnalysisController implements IAnalysisController {

	private final File file;
	private final HybridInterpreter runtime;
	private final NewInputTermBuilder termBuilder;

	private IStrategoTerm ast;

	public SingleFileCachingAnalysisController(File file) {
		assert file != null;
		this.file = file;
		this.runtime = RuntimeService.INSTANCE().getRuntime(file);
		assert this.runtime != null;
		this.termBuilder = new NewInputTermBuilder(this.runtime);
	}

	/**
	 * @see IAnalysisController#getFile()
	 */
	@Override
	public File getFile() {
		return this.file;
	}

	/**
	 * Returns the analyzed AST for the file, as cached. If no such AST has been cached yet, the
	 * file is first analyzed.
	 * 
	 * @see IAnalysisController#getAnalyzedAst()
	 */
	@Override
	public IStrategoTerm getAnalyzedAst() {
		if (ast == null) {
			analyze();
		}
		return ast;
	}

	private void analyze() {
		final ALanguage lang = LanguageService.INSTANCE().getLanguageByExten(file);
		if (lang == null) {
			reportError("No language for file " + file.getPath());
			return;
		}
		final String analyzeFunction = lang.getAnalysisFunction();
		assert analyzeFunction != null;
		final IStrategoTerm sourceAst = ParseService.INSTANCE().parse(file);
		final IStrategoTerm inputTerm = termBuilder.makeInputTerm(sourceAst, false, file);

		ast = null;
		// TODO invoke analysis function
		final IStrategoTerm feedback = null;

		if (feedback == null) {
			reportAnalysisFailed();
			return;
		} else {
			// TODO extract the relevant information from feedback: ast', messages
			// see StrategoObserver#presentToUser
		}

	}

	private void reportError(String msg) {
		final Message error = Message.newAnalysisErrorAtTop(file.getPath(), msg);
		MessageService.INSTANCE().addMessage(error);
	}

	private void reportAnalysisFailed() {
		final String log = ((LoggingIOAgent) runtime.getIOAgent()).getLog();
		final Message error = Message.newAnalysisErrorAtTop(file.getPath(), "Analysis failed: \n" + log);
		MessageService.INSTANCE().addMessage(error);
	}
}
