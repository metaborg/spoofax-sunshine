/**
 * 
 */
package org.spoofax.sunshine.pipeline;

import org.spoofax.sunshine.pipeline.diff.MultiDiff;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 *
 */
public interface ISinkMany<P> {
    public void sink(MultiDiff<P> product);
}
