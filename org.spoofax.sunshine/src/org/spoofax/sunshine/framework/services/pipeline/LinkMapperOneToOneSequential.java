/**
 * 
 */
package org.spoofax.sunshine.framework.services.pipeline;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.spoofax.sunshine.framework.services.pipeline.diff.Diff;
import org.spoofax.sunshine.framework.services.pipeline.diff.MultiDiff;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class LinkMapperOneToOneSequential<I, P> implements ISinkMany<I>,
	ISourceMany<P> {

    private final SinkAggregator<P> aggregator = new SinkAggregator<P>();

    private final ILinkOneToOne<I, P> link;

    private final Collection<ISinkMany<P>> sinks = new HashSet<ISinkMany<P>>();

    public LinkMapperOneToOneSequential(ILinkOneToOne<I, P> link) {
	this.link = link;
	this.link.addSink(aggregator);
    }

    @Override
    public void addSink(ISinkMany<P> sink) {
	sinks.add(sink);
    }

    @Override
    public void sink(MultiDiff<I> product) {
	aggregator.start();
	final Iterator<Diff<I>> productIter = product.iterator();
	while (productIter.hasNext()) {
	    link.sink(productIter.next());
	}
	MultiDiff<P> aggregated = aggregator.stop();
	for (ISinkMany<P> sink : sinks) {
	    sink.sink(aggregated);
	}
    }

    private class SinkAggregator<PR> implements ISinkOne<PR> {

	private MultiDiff<PR> aggregated;

	public void start() {
	    aggregated = new MultiDiff<PR>();
	}

	public MultiDiff<PR> stop() {
	    final MultiDiff<PR> tmp = aggregated;
	    aggregated = null;
	    return tmp;
	}

	@Override
	public void sink(Diff<PR> product) {
	    assert aggregated != null;
	    aggregated.add(product);
	}

    }

}
