/**
 * 
 */
package org.metaborg.sunshine.services.filesource;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metaborg.sunshine.pipeline.diff.Diff;
import org.metaborg.sunshine.pipeline.diff.DiffKind;
import org.metaborg.sunshine.pipeline.diff.MultiDiff;
import org.metaborg.sunshine.util.DiffingHashMap;

import com.google.common.collect.Iterables;

/**
 * This class "monitors" a project directory for changes. Everytime it is poked
 * for changes it
 * returns the ADDITIONS, REMOVALS and MODIFICATIONS since the last time it has
 * been poked. The
 * first time it has been poked it returns all files in the directory as
 * ADDITION.
 * 
 * It survives past restarts of the JVM by persisting changes in the cache
 * folder of the project is
 * so enabled.
 * 
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class DirMonitor {
	private static final Logger logger = LogManager.getLogger(DirMonitor.class
			.getName());

	private final File cacheFile;
	private final File dir;
	private final Iterable<String> extensions;
	private final DiffingHashMap<String, FileModifiedPair> store;

	public void reset() {
		store.clear();
		try {
			writeToPersist();
		} catch (IOException e) {
			logger.warn("Could not write to cache after clear", e);
		}
	}

	public DirMonitor(Iterable<String> extensions, File dir, File cacheDir) {
		assert dir != null;
		if (dir == null || !dir.exists()) {
			throw new IllegalArgumentException("Cannot monitor a directory ("
					+ dir + ") which does not exist");
		}
		assert cacheDir != null;
		if (cacheDir == null) {
			throw new IllegalArgumentException("The cache directory is null");
		}
		if (!cacheDir.exists()) {
			cacheDir.mkdir();
		}
		this.cacheFile = new File(cacheDir, "fsmonitor.bin");
		this.dir = dir;
		this.extensions = extensions;
		this.store = new DiffingHashMap<String, FileModifiedPair>(
				new FileModifiedPairMerger());
		logger.trace("Monitor initialized for directory {} with extensions {}",
				dir, extensions);
		try {
			logger.trace("Reading store from cache");
			int read = loadFromPersist();
			logger.trace("Read {} entries from cached store", read);
		} catch (IOException ioex) {
			logger.warn("Failed to read from persisted store", ioex);
		}
	}

	private Map<String, DiffKind> updateStore() {
		logger.trace("Beginning store diff");
		store.beginDiff();
		Iterator<File> fileIter = FileUtils.iterateFiles(dir,
				Iterables.toArray(extensions, String.class), true);
		while (fileIter.hasNext()) {
			File f = fileIter.next();
			store.put(f.getPath(), new FileModifiedPair(f));
			logger.trace("Stored {}", f.getPath());
		}
		logger.trace("Done store diffing");
		Map<String, DiffKind> diff = store.endDiff();
		if (diff.size() > 0) {
			try {
				logger.trace("Persisting store to cache");
				int bytes = writeToPersist();
				logger.trace("Store persisted using {} bytes", bytes);
			} catch (IOException ioex) {
				logger.warn("Persisting store failed", ioex);
			}
		}
		return diff;
	}

	public MultiDiff<File> getChanges() {
		logger.trace("Retrieving changes in dir {}", dir);
		MultiDiff<File> diff = new MultiDiff<File>();
		Map<String, DiffKind> changes = updateStore();
		logger.trace("Changes are {}", changes.size());
		for (Entry<String, DiffKind> ch : changes.entrySet()) {
			switch (ch.getValue()) {
			case ADDITION:
				logger.trace("ADDITION of {}", ch.getKey());
				diff.add(new Diff<File>(new File(ch.getKey()),
						DiffKind.ADDITION));
				break;
			case MODIFICATION:
				logger.trace("MODIFICATION of {}", ch.getKey());
				diff.add(new Diff<File>(new File(ch.getKey()),
						DiffKind.MODIFICATION));
				break;
			case DELETION:
				logger.trace("DELETION of {}", ch.getKey());
				diff.add(new Diff<File>(new File(ch.getKey()),
						DiffKind.DELETION));
				break;
			}
		}
		logger.trace("Returning changes");
		return diff;
	}

	private int writeToPersist() throws IOException {
		if (!cacheFile.exists())
			cacheFile.createNewFile();
		DataOutputStream dos = new DataOutputStream(new FileOutputStream(
				cacheFile, false));
		Set<Entry<String, FileModifiedPair>> entries = store.entrySet();
		try {
			for (Entry<String, FileModifiedPair> entry : entries) {
				dos.writeUTF(entry.getKey());
				dos.writeUTF(entry.getValue().getFile().getAbsolutePath());
				dos.writeLong(entry.getValue().getModified());
			}
			dos.flush();
		} finally {
			dos.close();
		}
		return dos.size();
	}

	private int loadFromPersist() throws IOException {
		if (!cacheFile.exists())
			return 0;
		DataInputStream dis = new DataInputStream(
				new FileInputStream(cacheFile));
		int read = 0;
		try {
			while (dis.available() > 0) {
				String key = dis.readUTF();
				File f = new File(dis.readUTF());
				long mod = dis.readLong();
				store.put(key, new FileModifiedPair(f, mod));
				read++;
			}
		} finally {
			dis.close();
		}
		return read;
	}

}
