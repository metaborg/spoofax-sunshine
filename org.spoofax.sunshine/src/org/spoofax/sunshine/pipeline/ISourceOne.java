/**
 * 
 */
package org.spoofax.sunshine.pipeline;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public interface ISourceOne<P> {

	public void addSink(ISinkOne<P> sink);
}
