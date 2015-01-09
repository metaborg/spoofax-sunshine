/**
 * 
 */
package org.metaborg.sunshine.services.filesource;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metaborg.spoofax.core.language.AllLanguagesFileSelector;
import org.metaborg.spoofax.core.language.ILanguageIdentifierService;
import org.metaborg.spoofax.core.language.ILanguageService;
import org.metaborg.spoofax.core.resource.IResourceChange;
import org.metaborg.spoofax.core.resource.IResourceService;
import org.metaborg.spoofax.core.resource.OfflineResourceChangeMonitor;
import org.metaborg.sunshine.environment.LaunchConfiguration;
import org.metaborg.sunshine.environment.ServiceRegistry;
import org.metaborg.sunshine.pipeline.ISinkMany;
import org.metaborg.sunshine.pipeline.ISourceMany;
import org.metaborg.sunshine.pipeline.diff.Diff;
import org.metaborg.sunshine.pipeline.diff.DiffKind;
import org.metaborg.sunshine.pipeline.diff.MultiDiff;
import org.metaborg.sunshine.statistics.BoxValidatable;
import org.metaborg.sunshine.statistics.Statistics;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class FileSource implements ISourceMany<FileObject> {
	private static final Logger logger = LogManager.getLogger(FileSource.class
			.getName());

	private final Collection<ISinkMany<FileObject>> sinks;
	private final OfflineResourceChangeMonitor monitor;

	public FileSource(FileObject directory, ILanguageService languageService) {
		this.sinks = new HashSet<ISinkMany<FileObject>>();
		final ServiceRegistry services = ServiceRegistry.INSTANCE();
		final IResourceService resourceService = services
				.getService(IResourceService.class);
		final ILanguageIdentifierService languageIdentifierService = services
				.getService(ILanguageIdentifierService.class);
		this.monitor = new OfflineResourceChangeMonitor(resourceService.root(),
				resourceService.userStorage(), new AllLanguagesFileSelector(
						languageIdentifierService), resourceService);
		try {
			monitor.read();
		} catch (FileSystemException e) {
			logger.warn("Could not read previous resources into the directory monitor");
		}
	}

	@Override
	public void addSink(ISinkMany<FileObject> sink) {
		sinks.add(sink);
	}

	public void poke() throws IOException {
		logger.trace("Poked for changes");
		if (ServiceRegistry.INSTANCE().getService(LaunchConfiguration.class).mainArguments.nonincremental) {
			logger.warn("Resetting the directory monitor for full analysis");
			monitor.reset();
		}
		logger.debug("Getting directory changes");
		final Iterable<IResourceChange> changes = monitor.update();
		monitor.write();
		final MultiDiff<FileObject> diff = createDiff(changes);
		logger.debug("Notifying {} sinks of {} file changes", sinks.size(),
				diff.size());
		Statistics.addDataPoint("DELTAFILES",
				new BoxValidatable<Integer>(diff.size()));
		for (ISinkMany<FileObject> sink : sinks) {
			sink.sink(diff);
		}
		logger.trace("Done sinking");
	}

	public MultiDiff<FileObject> createDiff(Iterable<IResourceChange> changes) {
		final MultiDiff<FileObject> diff = new MultiDiff<FileObject>();
		for (IResourceChange change : changes) {
			switch (change.kind()) {
			case Create:
				diff.add(new Diff<FileObject>(change.resource(),
						DiffKind.ADDITION));
				break;
			case Delete:
				diff.add(new Diff<FileObject>(change.resource(),
						DiffKind.DELETION));
				break;
			case Modify:
				diff.add(new Diff<FileObject>(change.resource(),
						DiffKind.MODIFICATION));
				break;
			case Rename:
				if (change.renamedFrom() != null) {
					diff.add(new Diff<FileObject>(change.resource(),
							DiffKind.ADDITION));
				}
				if (change.renamedTo() != null) {
					diff.add(new Diff<FileObject>(change.resource(),
							DiffKind.DELETION));
				}
				break;
			}
		}
		return diff;
	}
}
