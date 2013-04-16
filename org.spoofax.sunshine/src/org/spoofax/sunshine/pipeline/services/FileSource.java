/**
 * 
 */
package org.spoofax.sunshine.pipeline.services;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spoofax.sunshine.Environment;
import org.spoofax.sunshine.pipeline.ISinkMany;
import org.spoofax.sunshine.pipeline.ISourceMany;
import org.spoofax.sunshine.pipeline.diff.MultiDiff;
import org.spoofax.sunshine.services.LanguageService;
import org.spoofax.sunshine.services.files.DirMonitor;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class FileSource implements ISourceMany<File> {
    private static final Logger logger = LogManager.getLogger(FileSource.class
	    .getName());

    private final Collection<ISinkMany<File>> sinks;
    private final DirMonitor monitor;

    public FileSource(File directory) {
	this.sinks = new HashSet<ISinkMany<File>>();
	this.monitor = new DirMonitor(LanguageService.INSTANCE()
		.getSupportedExtens(), directory, Environment.INSTANCE()
		.getCacheDir());
    }

    @Override
    public void addSink(ISinkMany<File> sink) {
	sinks.add(sink);
    }

    public void poke() {
	logger.trace("Poked for changes");

	logger.warn("Resetting the directory monitor (for debugging purposes)");
	monitor.reset();
	logger.debug("Getting directory changes");
	MultiDiff<File> diff = monitor.getChanges();
	logger.debug("Notifying {} sinks of {} file changes", sinks.size(),
		diff.size());
	for (ISinkMany<File> sink : sinks) {
	    sink.sink(diff);
	}
	logger.trace("Done sinking");
    }
}
