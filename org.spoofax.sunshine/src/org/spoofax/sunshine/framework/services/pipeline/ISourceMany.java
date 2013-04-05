/**
 * 
 */
package org.spoofax.sunshine.framework.services.pipeline;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public interface ISourceMany<P> {
    public void addSink(ISinkMany<P> sink);
}
