/**
 * 
 */
package org.spoofax.sunshine.framework.services.pipeline;

import java.util.Collection;
import java.util.HashSet;

import org.spoofax.sunshine.framework.services.pipeline.diff.Diff;
import org.spoofax.sunshine.framework.services.pipeline.diff.MultiDiff;


/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 *
 */
public abstract class ALinkOneToManySequential<I, P> implements
	ILinkOneToMany<I, P> {

    private final Collection<ISinkMany<P>> sinks = new HashSet<ISinkMany<P>>();

    @Override
    public void sink(Diff<I> product) {
	final MultiDiff<P> result = sinkWork(product);
	for (ISinkMany<P> sink : sinks) {
	    sink.sink(result);
	}
    }

    @Override
    public void addSink(ISinkMany<P> sink) {
	sinks.add(sink);
    }

    public abstract MultiDiff<P> sinkWork(Diff<I> input);
}
