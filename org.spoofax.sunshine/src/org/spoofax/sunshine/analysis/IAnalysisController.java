/**
 * 
 */
package org.spoofax.sunshine.analysis;

import java.io.File;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.sunshine.parser.framework.IParseController;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public interface IAnalysisController {
	File getFile();

	IStrategoTerm getAnalyzedAst();
	
}
