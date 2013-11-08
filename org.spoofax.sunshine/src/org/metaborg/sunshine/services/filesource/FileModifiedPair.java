/**
 * 
 */
package org.metaborg.sunshine.services.filesource;

import java.io.File;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class FileModifiedPair {

	private final long mod;
	private final File f;

	public FileModifiedPair(File file, long modified) {
		f = file;
		mod = modified;
	}

	public FileModifiedPair(File file) {
		f = file;
		mod = file.lastModified();
	}

	public File getFile() {
		return f;
	}

	public long getModified() {
		return mod;
	}

}
