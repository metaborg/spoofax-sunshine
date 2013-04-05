/**
 * 
 */
package org.spoofax.sunshine.framework.services.pipeline;

import java.util.Collection;
import java.util.HashSet;

import org.spoofax.sunshine.framework.services.pipeline.diff.Diff;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public abstract class ALinkOneToOneSequential<I, P> implements
	ILinkOneToOne<I, P> {

    private final Collection<ISinkOne<P>> sinks = new HashSet<ISinkOne<P>>();

    @Override
    public void addSink(ISinkOne<P> sink) {
	sinks.add(sink);
    }

    @Override
    public void sink(Diff<I> product) {
	final Diff<P> result = sinkWork(product);
	for (ISinkOne<P> sink : sinks) {
	    sink.sink(result);
	}
    }

    public abstract Diff<P> sinkWork(Diff<I> input);
}
