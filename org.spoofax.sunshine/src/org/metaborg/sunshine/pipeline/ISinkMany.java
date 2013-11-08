/**
 * 
 */
package org.metaborg.sunshine.pipeline;

import org.metaborg.sunshine.pipeline.diff.MultiDiff;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public interface ISinkMany<P> extends ISink<P> {
	public void sink(MultiDiff<P> product);
}
