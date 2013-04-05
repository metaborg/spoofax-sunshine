/**
 * 
 */
package org.spoofax.sunshine.framework.services.pipeline;


/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 *
 */
public interface ISourceOne<P> {

    public void addSink(ISinkOne<P> sink);
}
