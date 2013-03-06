/**
 * 
 */
package org.spoofax.sunshine.analysis;

import java.io.File;

import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * Interface for analysis controllers. All controllers that provide analysis services must implement
 * this interface. An analysis controller is bound to a particular file. Controllers may or not
 * cache analysis results.
 * 
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public interface IAnalysisController {

	/**
	 * @return The file that this controller is bound to.
	 */
	File getFile();

	/**
	 * Retrieves the analyzed AST as obtained after analysis of the file this controller is bound
	 * to. This interface imposes no restrictions on how implementors obtain analyzed ASTs. 
	 * 
	 * @return
	 * 	The analyzed AST if analysis could be completed, <code>null</code> otherwise.
	 */
	IStrategoTerm getAnalyzedAst();

}
