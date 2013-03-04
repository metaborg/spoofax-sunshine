/**
 * 
 */
package org.spoofax.sunshine.analysis;

import java.io.File;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.sunshine.framework.services.ParseService;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 *
 */
public class DummyAnalysisController implements IAnalysisController {

	public final File file;
	
	public DummyAnalysisController(File f){
		this.file = f;
	}
	
	@Override
	public File getFile() {
		return this.file;
	}

	@Override
	public IStrategoTerm getAnalyzedAst() {
		return ParseService.INSTANCE().parse(file);
	}

}
