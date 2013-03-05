/**
 * 
 */
package org.spoofax.sunshine.framework.services;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class AnalysisException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6066166359147108464L;

	public AnalysisException(String msg) {
		super(msg);
	}

	public AnalysisException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public AnalysisException(Throwable cause) {
		super(cause);
	}
}
