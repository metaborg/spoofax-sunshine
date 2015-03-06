package org.metaborg.sunshine.pipeline.connectors;

import java.util.Collection;
import java.util.HashSet;

import org.metaborg.sunshine.pipeline.ILinkManyToOne;
import org.metaborg.sunshine.pipeline.ISinkOne;
import org.metaborg.sunshine.pipeline.diff.Diff;
import org.metaborg.sunshine.pipeline.diff.MultiDiff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ALinkManyToOne<I, P> implements ILinkManyToOne<I, P> {
    private static final Logger logger = LoggerFactory.getLogger(ALinkManyToOne.class.getName());

    private final Collection<ISinkOne<P>> sinks = new HashSet<ISinkOne<P>>();

    @Override public void addSink(ISinkOne<P> sink) {
        assert sink != null;
        sinks.add(sink);
    }

    @Override public void sink(MultiDiff<I> product) {
        assert product != null;
        logger.trace("Sinking work for product");
        final Diff<P> result = sinkWork(product);
        logger.trace("Sinking changes to {} sinks", sinks.size());
        for(ISinkOne<P> sink : sinks) {
            logger.trace("Now sinking to sink {}", sink);
            sink.sink(result);
        }
    }

    public abstract Diff<P> sinkWork(MultiDiff<I> input);
}
