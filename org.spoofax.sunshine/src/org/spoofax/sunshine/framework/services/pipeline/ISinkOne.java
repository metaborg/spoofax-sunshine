/**
 * 
 */
package org.spoofax.sunshine.framework.services.pipeline;

import org.spoofax.sunshine.framework.services.pipeline.diff.Diff;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 *
 */
public interface ISinkOne<I> {
    public void sink(Diff<I> product);
}
