/**
 * 
 */
package org.spoofax.sunshine.services.filesource;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spoofax.sunshine.drivers.SunshineCLIEntry;
import org.spoofax.sunshine.pipeline.diff.Diff;
import org.spoofax.sunshine.pipeline.diff.DiffKind;
import org.spoofax.sunshine.pipeline.diff.MultiDiff;
import org.spoofax.sunshine.util.DiffingHashMap;

/**
 * This class "monitors" a project directory for changes. Everytime it is poked
 * for changes it returns the ADDITIONS, REMOVALS and MODIFICATIONS since the
 * last time it has been poked. The first time it has been poked it returns all
 * files in the directory as ADDITION.
 * 
 * It survives past restarts of the JVM by persisting changes in the cache
 * folder of the project is so enabled.
 * 
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class DirMonitor {
    private static final Logger logger = LogManager
	    .getLogger(SunshineCLIEntry.class.getName());

    private final File cacheFile;
    private final File dir;
    private final Collection<String> extensions;
    private final DiffingHashMap<String, FileModifiedPair> store;

    public void reset() {
	store.clear();
	try {
	    writeToPersist();
	} catch (IOException e) {
	    logger.warn("Could not write to cache after clear", e);
	}
    }

    public DirMonitor(Collection<String> extensions, File dir, File cacheDir) {
	assert dir != null;
	if (dir == null || !dir.exists()) {
	    throw new IllegalArgumentException("Cannot monitor a directory ("
		    + dir + ") which does not exist");
	}
	assert cacheDir != null;
	if (cacheDir == null || !cacheDir.exists()) {
	    throw new IllegalArgumentException("The cache directory ("
		    + cacheDir + ") does not exist");
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
		extensions.toArray(new String[extensions.size()]), true);
	while (fileIter.hasNext()) {
	    File f = fileIter.next();
	    store.put(f.getPath(), new FileModifiedPair(f));
	    logger.trace("Stored {}", f.getPath());
	}
	logger.trace("Done store diffing");
	try {
	    logger.trace("Persisting store to cache");
	    int bytes = writeToPersist();
	    logger.trace("Store persisted using {} bytes", bytes);
	} catch (IOException ioex) {
	    logger.warn("Persisting store failed", ioex);
	}
	return store.endDiff();
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
