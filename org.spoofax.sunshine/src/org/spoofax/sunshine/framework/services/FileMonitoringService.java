/**
 * 
 */
package org.spoofax.sunshine.framework.services;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AgeFileFilter;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.spoofax.sunshine.Environment;
import org.spoofax.sunshine.framework.language.ALanguage;

/**
 * A service which (synchronously) monitors files on the file system. When asked for files that have
 * changed this service returns a list of files that have changed since the last query. This service
 * only monitors changes to files with extensions that are provided by some language in the language
 * registry.
 * 
 * FIXME how to handle file deletions or file renaming?
 * 
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class FileMonitoringService {
	private static final int INITIAL_CUTOFF = -1;
	
	private static FileMonitoringService INSTANCE;

	private final IOFileFilter extensFilter;
	private long cutoffTime;

	private FileMonitoringService() {
		final Set<String> extensSet = LanguageService.INSTANCE().getSupportedExtens();
		final String[] extens = extensSet.toArray(new String[extensSet.size()]);
		for (int idx = 0; idx < extens.length; idx++) {
			extens[idx] = "." + extens[idx];
		}
		extensFilter = new SuffixFileFilter(extens);
		cutoffTime = INITIAL_CUTOFF;
	}

	public static FileMonitoringService INSTANCE() {
		if (INSTANCE == null) {
			INSTANCE = new FileMonitoringService();
		}
		return INSTANCE;
	}

	public void reset() {
		cutoffTime = INITIAL_CUTOFF;
	}

	/**
	 * Retrieve a collection of files that have changed, been created since the last time this
	 * method was called. The resulting collection contains only files (no directories) and these
	 * files all have extensions that have corresponding registered languages in
	 * {@link LanguageService}. Therefore reported files are guaranteed to have a {@link ALanguage}
	 * that can handle them.
	 * 
	 * @return A collection of {@link File} that have changed since last call. All files are
	 *         relative to {@link Environment#projectDir} but are not parented to it, i.e.
	 *         {@link File#exists()} will return false unless the working directory is
	 *         {@link Environment#projectDir}.
	 */
	public Collection<File> getChanges() {
		final File projectDir = Environment.INSTANCE().projectDir;
		assert projectDir.isDirectory();
		final IOFileFilter filter = new AndFileFilter(new AgeFileFilter(cutoffTime, false), extensFilter);
		final Iterator<File> fileIter = FileUtils.iterateFiles(projectDir, filter, TrueFileFilter.INSTANCE);
		final Collection<File> files = new LinkedList<File>();
		long newestAge = cutoffTime;
		while (fileIter.hasNext()) {
			final File af = fileIter.next();
			File rf = new File(projectDir.toURI().relativize(af.toURI()).toString());
			files.add(rf);
			final long modtime = af.lastModified();
			if (modtime > newestAge) {
				newestAge = modtime;
			}
		}
		cutoffTime = newestAge;
		return files;
	}

	/**
	 * Same as {@link #getChanges()} with the exception that the cached cutoff time is not cached. A
	 * subsequent call to {@link #getChanges()} will report at least the same files as the
	 * preeceding call to this method.
	 * 
	 * @see #getChanges()
	 */
	public Collection<File> getChangesNoPersist() {
		final long savedCutOffTime = cutoffTime;
		final Collection<File> files = getChanges();
		cutoffTime = savedCutOffTime;
		return files;
	}

}
