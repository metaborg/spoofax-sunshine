/**
 * 
 */
package org.metaborg.sunshine.pipeline.connectors;

import java.util.Collection;
import java.util.HashSet;

import org.metaborg.sunshine.pipeline.ILinkOneToOne;
import org.metaborg.sunshine.pipeline.ISinkOne;
import org.metaborg.sunshine.pipeline.diff.Diff;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public abstract class ALinkOneToOne<I, P> implements ILinkOneToOne<I, P> {

    private final Collection<ISinkOne<P>> sinks = new HashSet<ISinkOne<P>>();

    @Override public void addSink(ISinkOne<P> sink) {
        sinks.add(sink);
    }

    @Override public void sink(Diff<I> product) {
        final Diff<P> result = sinkWork(product);
        for(ISinkOne<P> sink : sinks) {
            sink.sink(result);
        }
    }

    public abstract Diff<P> sinkWork(Diff<I> input);
}
