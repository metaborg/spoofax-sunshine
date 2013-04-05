/**
 * 
 */
package org.spoofax.sunshine.pipeline.connectors;

import java.util.Collection;
import java.util.HashSet;

import org.spoofax.sunshine.pipeline.ILinkManyToMany;
import org.spoofax.sunshine.pipeline.ISinkMany;
import org.spoofax.sunshine.pipeline.diff.MultiDiff;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public abstract class ALinkManyToMany<I, P> implements ILinkManyToMany<I, P> {

    private final Collection<ISinkMany<P>> sinks = new HashSet<ISinkMany<P>>();

    @Override
    public void addSink(ISinkMany<P> sink) {
	sinks.add(sink);
    }

    @Override
    public void sink(MultiDiff<I> product) {
	final MultiDiff<P> result = sinkWork(product);
	for (ISinkMany<P> sink : sinks) {
	    sink.sink(result);
	}
    }

    public abstract MultiDiff<P> sinkWork(MultiDiff<I> input);

}
