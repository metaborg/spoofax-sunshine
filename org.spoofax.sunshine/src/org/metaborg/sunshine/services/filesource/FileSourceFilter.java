/**
 * 
 */
package org.metaborg.sunshine.services.filesource;

import java.io.File;

import org.apache.commons.vfs2.FileObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metaborg.sunshine.pipeline.connectors.ALinkManyToMany;
import org.metaborg.sunshine.pipeline.diff.Diff;
import org.metaborg.sunshine.pipeline.diff.MultiDiff;

/**
 * @author vladvergu
 * 
 */
public class FileSourceFilter extends ALinkManyToMany<FileObject, FileObject> {

	private static final Logger logger = LogManager.getLogger(FileSource.class
			.getName());

	private final String regex;

	/**
	 * Constructs a {@link FileSourceFilter} that will only pass through
	 * instances of {@link File} whose absolute path returned by
	 * {@link File#getAbsolutePath()} matches the given regular
	 * expression. If the given regex is <code>null</code> then all files are
	 * passed through.
	 * 
	 * @param regex
	 */
	public FileSourceFilter(String regex) {
		this.regex = regex;
		logger.trace("FileSourceFilter initialized to allow files matching {}",
				regex);
	}

	@Override
	public MultiDiff<FileObject> sinkWork(MultiDiff<FileObject> input) {
		if (regex == null) {
			logger.trace("Passing all files through");
			return input;
		}
		MultiDiff<FileObject> output = new MultiDiff<FileObject>();
		for (Diff<FileObject> diff : input) {
			final String path = diff.getPayload().getName().getPath();
			if (path.matches(regex)) {
				logger.trace("Passing file {} to sinks", path);
				output.add(diff);
			}
		}
		return output;
	}
}
