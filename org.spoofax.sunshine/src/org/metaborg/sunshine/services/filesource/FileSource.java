/**
 * 
 */
package org.metaborg.sunshine.services.filesource;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metaborg.spoofax.core.language.ILanguageService;
import org.metaborg.sunshine.environment.LaunchConfiguration;
import org.metaborg.sunshine.environment.ServiceRegistry;
import org.metaborg.sunshine.pipeline.ISinkMany;
import org.metaborg.sunshine.pipeline.ISourceMany;
import org.metaborg.sunshine.pipeline.diff.MultiDiff;
import org.metaborg.sunshine.statistics.BoxValidatable;
import org.metaborg.sunshine.statistics.Statistics;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class FileSource implements ISourceMany<File> {
	private static final Logger logger = LogManager.getLogger(FileSource.class
			.getName());

	private final Collection<ISinkMany<File>> sinks;
	private final DirMonitor monitor;

	public FileSource(File directory, ILanguageService languageService) {
		this.sinks = new HashSet<ISinkMany<File>>();
		ServiceRegistry services = ServiceRegistry.INSTANCE();
		this.monitor = new DirMonitor(languageService.getSupportedExt(),
				directory, services.getService(LaunchConfiguration.class)
						.getCacheDir());
	}

	@Override
	public void addSink(ISinkMany<File> sink) {
		sinks.add(sink);
	}

	public void poke() {
		logger.trace("Poked for changes");
		if (ServiceRegistry.INSTANCE().getService(LaunchConfiguration.class).mainArguments.nonincremental) {
			logger.warn("Resetting the directory monitor for full analysis");
			monitor.reset();
		}
		logger.debug("Getting directory changes");
		MultiDiff<File> diff = monitor.getChanges();
		logger.debug("Notifying {} sinks of {} file changes", sinks.size(),
				diff.size());
		Statistics.addDataPoint("DELTAFILES",
				new BoxValidatable<Integer>(diff.size()));
		for (ISinkMany<File> sink : sinks) {
			sink.sink(diff);
		}
		logger.trace("Done sinking");
	}
}
