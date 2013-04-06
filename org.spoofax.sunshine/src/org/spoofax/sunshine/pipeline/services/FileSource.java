/**
 * 
 */
package org.spoofax.sunshine.pipeline.services;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;

import org.spoofax.sunshine.pipeline.ISinkMany;
import org.spoofax.sunshine.pipeline.ISourceMany;
import org.spoofax.sunshine.pipeline.diff.Diff;
import org.spoofax.sunshine.pipeline.diff.DiffKind;
import org.spoofax.sunshine.pipeline.diff.MultiDiff;
import org.spoofax.sunshine.services.old.FileMonitoringService;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class FileSource implements ISourceMany<File> {

    private final Collection<ISinkMany<File>> sinks = new HashSet<ISinkMany<File>>();

    @Override
    public void addSink(ISinkMany<File> sink) {
	sinks.add(sink);
    }

    public void kick() {
	FileMonitoringService.INSTANCE().reset();
	Collection<File> filesChanged = FileMonitoringService.INSTANCE()
		.getChanges();
	MultiDiff<File> diff = new MultiDiff<File>();
	for (File f : filesChanged) {
	    diff.add(new Diff<File>(f, DiffKind.ADDITION));
	}
	System.err.println("FileSource notifying " + sinks.size()
		+ " sinks of " + diff.size() + " file changes");
	for (ISinkMany<File> sink : sinks) {
	    sink.sink(diff);
	}
    }

}
