/**
 * 
 */
package org.spoofax.sunshine;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class CompilerException extends RuntimeException {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1786746541057331233L;

    public CompilerException(String msg) {
	super(msg);
    }

    public CompilerException(String msg, Throwable t) {
	super(msg, t);
    }

    public CompilerException(Throwable t) {
	super(t);
    }
}
