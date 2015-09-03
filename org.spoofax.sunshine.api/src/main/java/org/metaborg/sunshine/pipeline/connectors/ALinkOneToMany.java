/**
 * 
 */
package org.metaborg.sunshine.pipeline.connectors;

import java.util.Collection;
import java.util.HashSet;

import org.metaborg.sunshine.pipeline.ILinkOneToMany;
import org.metaborg.sunshine.pipeline.ISinkMany;
import org.metaborg.sunshine.pipeline.diff.Diff;
import org.metaborg.sunshine.pipeline.diff.MultiDiff;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public abstract class ALinkOneToMany<I, P> implements ILinkOneToMany<I, P> {

    private final Collection<ISinkMany<P>> sinks = new HashSet<ISinkMany<P>>();

    @Override public void sink(Diff<I> product) {
        final MultiDiff<P> result = sinkWork(product);
        for(ISinkMany<P> sink : sinks) {
            sink.sink(result);
        }
    }

    @Override public void addSink(ISinkMany<P> sink) {
        sinks.add(sink);
    }

    public abstract MultiDiff<P> sinkWork(Diff<I> input);
}
