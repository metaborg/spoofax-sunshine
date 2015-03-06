/**
 * 
 */
package org.metaborg.sunshine.pipeline;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public interface ISourceMany<P> extends ISource<P> {
    public void addSink(ISinkMany<P> sink);
}
