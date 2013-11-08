/**
 * 
 */
package org.metaborg.sunshine.pipeline;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public interface ISourceOne<P> extends ISource<P> {

	public void addSink(ISinkOne<P> sink);
}
