/**
 * 
 */
package org.metaborg.sunshine.pipeline;

import org.metaborg.sunshine.pipeline.diff.Diff;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public interface ISinkOne<I> extends ISink<I> {
    public void sink(Diff<I> product);
}
