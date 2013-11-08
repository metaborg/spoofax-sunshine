/**
 * 
 */
package org.metaborg.sunshine.services.filesource;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metaborg.sunshine.pipeline.connectors.ALinkManyToMany;
import org.metaborg.sunshine.pipeline.diff.Diff;
import org.metaborg.sunshine.pipeline.diff.MultiDiff;

/**
 * @author vladvergu
 * 
 */
public class FileSourceFilter extends ALinkManyToMany<File, File> {

	private static final Logger logger = LogManager.getLogger(FileSource.class.getName());

	private final String regex;

	/**
	 * Constructs a {@link FileSourceFilter} that will only pass through instances of {@link File}
	 * whose absolute path returned by {@link File#getAbsolutePath()} matches the given regular
	 * expression. If the given regex is <code>null</code> then all files are passed through.
	 * 
	 * @param regex
	 */
	public FileSourceFilter(String regex) {
		this.regex = regex;
		logger.trace("FileSourceFilter initialized to allow files matching {}", regex);
	}

	@Override
	public MultiDiff<File> sinkWork(MultiDiff<File> input) {
		if (regex == null) {
			logger.trace("Passing all files through");
			return input;
		}
		MultiDiff<File> output = new MultiDiff<File>();
		for (Diff<File> diff : input) {
			if (diff.getPayload().getAbsolutePath().matches(regex)) {
				logger.trace("Passing file {} to sinks", diff.getPayload().getAbsolutePath());
				output.add(diff);
			}
		}
		return output;
	}

}
