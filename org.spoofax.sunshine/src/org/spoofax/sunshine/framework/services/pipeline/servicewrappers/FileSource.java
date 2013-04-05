/**
 * 
 */
package org.spoofax.sunshine.framework.services.pipeline.servicewrappers;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;

import org.spoofax.sunshine.framework.services.FileMonitoringService;
import org.spoofax.sunshine.framework.services.pipeline.ISinkMany;
import org.spoofax.sunshine.framework.services.pipeline.ISourceMany;
import org.spoofax.sunshine.framework.services.pipeline.diff.Diff;
import org.spoofax.sunshine.framework.services.pipeline.diff.DiffKind;
import org.spoofax.sunshine.framework.services.pipeline.diff.MultiDiff;

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
	Collection<File> filesChanged = FileMonitoringService.INSTANCE()
		.getChanges();
	MultiDiff<File> diff = new MultiDiff<File>();
	for (File f : filesChanged) {
	    diff.add(new Diff<File>(f, DiffKind.ADDITION));
	}
	for (ISinkMany<File> sink : sinks) {
	    sink.sink(diff);
	}
    }

}
