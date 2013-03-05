/**
 * 
 */
package org.spoofax.sunshine.framework.messages;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public abstract class ARegion {
	
	protected static final String COMMA = ",";
	protected static final String COLON = ":";
	
	@Override
	public abstract String toString();

	@Override
	public abstract boolean equals(Object o);

	@Override
	public abstract int hashCode();

}
