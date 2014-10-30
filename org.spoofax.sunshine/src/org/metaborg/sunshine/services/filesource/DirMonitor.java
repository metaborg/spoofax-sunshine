/**
 * 
 */
package org.metaborg.sunshine.services.filesource;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metaborg.sunshine.pipeline.diff.Diff;
import org.metaborg.sunshine.pipeline.diff.DiffKind;
import org.metaborg.sunshine.pipeline.diff.MultiDiff;
import org.metaborg.sunshine.util.DiffingHashMap;

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

	private final FileObject cacheFile;
	private final FileObject dir;
	private final FileSelector selector;
	private final FileSystemManager fsm;
	private final DiffingHashMap<String, FileModifiedPair> store;

	public DirMonitor(FileObject dir, FileObject cacheDir,
			FileSelector selector, FileSystemManager fsm) {
		try {
			if (dir == null || !dir.exists()) {
				throw new IllegalArgumentException(
						"Cannot monitor a directory (" + dir
								+ ") which does not exist");
			}
			assert cacheDir != null;
			if (cacheDir == null) {
				throw new IllegalArgumentException(
						"The cache directory is null");
			}
			if (!cacheDir.exists()) {
				cacheDir.createFolder();
			}
			this.cacheFile = cacheDir.resolveFile("fsmonitor.bin");
		} catch (FileSystemException e) {
			throw new RuntimeException(e);
		}

		this.dir = dir;
		this.selector = selector;
		this.fsm = fsm;
		this.store = new DiffingHashMap<String, FileModifiedPair>(
				new FileModifiedPairMerger());

		logger.trace("Monitor initialized for directory {}", dir);
		try {
			logger.trace("Reading store from cache");
			int read = loadFromPersist();
			logger.trace("Read {} entries from cached store", read);
		} catch (IOException ioex) {
			logger.warn("Failed to read from persisted store", ioex);
		}
	}

	private Map<String, DiffKind> updateStore() throws IOException {
		logger.trace("Beginning store diff");
		store.beginDiff();

		final FileObject[] files = dir.findFiles(selector);
		for (FileObject file : files) {
			store.put(file.getName().getPath(), new FileModifiedPair(file));
			logger.trace("Stored {}", file);
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

	public MultiDiff<FileObject> getChanges() throws IOException {
		logger.trace("Retrieving changes in dir {}", dir);
		MultiDiff<FileObject> diff = new MultiDiff<FileObject>();
		Map<String, DiffKind> changes = updateStore();
		logger.trace("Changes are {}", changes.size());
		for (Entry<String, DiffKind> ch : changes.entrySet()) {
			switch (ch.getValue()) {
			case ADDITION:
				logger.trace("ADDITION of {}", ch.getKey());
				diff.add(new Diff<FileObject>(fsm.resolveFile(ch.getKey()),
						DiffKind.ADDITION));
				break;
			case MODIFICATION:
				logger.trace("MODIFICATION of {}", ch.getKey());
				diff.add(new Diff<FileObject>(fsm.resolveFile(ch.getKey()),
						DiffKind.MODIFICATION));
				break;
			case DELETION:
				logger.trace("DELETION of {}", ch.getKey());
				diff.add(new Diff<FileObject>(fsm.resolveFile(ch.getKey()),
						DiffKind.DELETION));
				break;
			}
		}
		logger.trace("Returning changes");
		return diff;
	}

	private int writeToPersist() throws IOException {
		if (!cacheFile.exists())
			cacheFile.createFile();

		final DataOutputStream dos = new DataOutputStream(cacheFile
				.getContent().getOutputStream());
		final Set<Entry<String, FileModifiedPair>> entries = store.entrySet();
		try {
			for (Entry<String, FileModifiedPair> entry : entries) {
				dos.writeUTF(entry.getKey());
				dos.writeUTF(entry.getValue().getFile().getName().getPath());
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
		final DataInputStream dis = new DataInputStream(cacheFile.getContent()
				.getInputStream());
		int read = 0;
		try {
			while (dis.available() > 0) {
				final String key = dis.readUTF();
				final FileObject f = fsm.resolveFile(dis.readUTF());
				long mod = dis.readLong();
				store.put(key, new FileModifiedPair(f, mod));
				read++;
			}
		} finally {
			dis.close();
		}
		return read;
	}

	public void reset() {
		store.clear();
		try {
			writeToPersist();
		} catch (IOException e) {
			logger.warn("Could not write to cache after clear", e);
		}
	}
}
