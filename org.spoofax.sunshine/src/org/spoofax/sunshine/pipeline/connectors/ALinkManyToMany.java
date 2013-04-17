/**
 * 
 */
package org.spoofax.sunshine.pipeline.connectors;

import java.util.Collection;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spoofax.sunshine.pipeline.ILinkManyToMany;
import org.spoofax.sunshine.pipeline.ISinkMany;
import org.spoofax.sunshine.pipeline.diff.MultiDiff;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public abstract class ALinkManyToMany<I, P> implements ILinkManyToMany<I, P> {
    private static final Logger logger = LogManager
	    .getLogger(ALinkManyToMany.class.getName());

    private final Collection<ISinkMany<P>> sinks = new HashSet<ISinkMany<P>>();

    @Override
    public void addSink(ISinkMany<P> sink) {
	assert sink != null;
	sinks.add(sink);
    }

    @Override
    public void sink(MultiDiff<I> product) {
	assert product != null;
	logger.trace("Sinking work for product");
	final MultiDiff<P> result = sinkWork(product);
	logger.trace("Sinking changes to {} sinks", sinks.size());
	for (ISinkMany<P> sink : sinks) {
	    logger.trace("Now sinking to sink {}", sink);
	    sink.sink(result);
	}
    }

    public abstract MultiDiff<P> sinkWork(MultiDiff<I> input);

}
