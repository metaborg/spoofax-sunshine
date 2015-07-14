package org.metaborg.sunshine.services.filesource;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.language.AllLanguagesFileSelector;
import org.metaborg.core.language.ILanguageIdentifierService;
import org.metaborg.core.resource.ResourceChange;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.core.resource.OfflineResourceChangeMonitor;
import org.metaborg.sunshine.environment.LaunchConfiguration;
import org.metaborg.sunshine.environment.ServiceRegistry;
import org.metaborg.sunshine.pipeline.ISinkMany;
import org.metaborg.sunshine.pipeline.ISourceMany;
import org.metaborg.sunshine.pipeline.diff.Diff;
import org.metaborg.sunshine.pipeline.diff.DiffKind;
import org.metaborg.sunshine.pipeline.diff.MultiDiff;
import org.metaborg.sunshine.statistics.BoxValidatable;
import org.metaborg.sunshine.statistics.Statistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileSource implements ISourceMany<FileObject> {
    private static final Logger logger = LoggerFactory.getLogger(FileSource.class.getName());

    private final Collection<ISinkMany<FileObject>> sinks;
    private final OfflineResourceChangeMonitor monitor;

    public FileSource(FileObject directory) {
        this.sinks = new HashSet<ISinkMany<FileObject>>();
        final ServiceRegistry services = ServiceRegistry.INSTANCE();
        final IResourceService resourceService = services.getService(IResourceService.class);
        final ILanguageIdentifierService languageIdentifierService =
            services.getService(ILanguageIdentifierService.class);
        this.monitor =
            new OfflineResourceChangeMonitor(directory, resourceService.userStorage(), new AllLanguagesFileSelector(
                languageIdentifierService), resourceService);
        try {
            monitor.read();
        } catch(FileSystemException e) {
            logger.warn("Could not read previous resources into the directory monitor");
        }
    }

    @Override public void addSink(ISinkMany<FileObject> sink) {
        sinks.add(sink);
    }

    public void poke() throws IOException {
        logger.trace("Poked for changes");
        if(ServiceRegistry.INSTANCE().getService(LaunchConfiguration.class).mainArguments.nonincremental) {
            logger.warn("Resetting the directory monitor for full analysis");
            monitor.reset();
        }
        logger.debug("Getting directory changes");
        final Iterable<ResourceChange> changes = monitor.update();
        monitor.write();
        final MultiDiff<FileObject> diff = createDiff(changes);
        logger.debug("Notifying {} sinks of {} file changes", sinks.size(), diff.size());
        Statistics.addDataPoint("DELTAFILES", new BoxValidatable<Integer>(diff.size()));
        for(ISinkMany<FileObject> sink : sinks) {
            sink.sink(diff);
        }
        logger.trace("Done sinking");
    }

    public MultiDiff<FileObject> createDiff(Iterable<ResourceChange> changes) {
        final MultiDiff<FileObject> diff = new MultiDiff<FileObject>();
        for(ResourceChange change : changes) {
            switch(change.kind) {
                case Create:
                    diff.add(new Diff<FileObject>(change.resource, DiffKind.ADDITION));
                    break;
                case Delete:
                    diff.add(new Diff<FileObject>(change.resource, DiffKind.DELETION));
                    break;
                case Modify:
                    diff.add(new Diff<FileObject>(change.resource, DiffKind.MODIFICATION));
                    break;
                case Rename:
                    if(change.from != null) {
                        diff.add(new Diff<FileObject>(change.resource, DiffKind.ADDITION));
                    }
                    if(change.to != null) {
                        diff.add(new Diff<FileObject>(change.resource, DiffKind.DELETION));
                    }
                    break;
            }
        }
        return diff;
    }
}
