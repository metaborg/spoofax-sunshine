/**
 * 
 */
package org.spoofax.sunshine.parser.framework;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class ParserException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1317748684651592920L;

	public ParserException() {
		super();
	}

	public ParserException(String msg) {
		super(msg);
	}

	public ParserException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public ParserException(Throwable cause) {
		super(cause);
	}

}
