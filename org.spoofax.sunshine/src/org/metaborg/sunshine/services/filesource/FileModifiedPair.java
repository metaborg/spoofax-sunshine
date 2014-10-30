/**
 * 
 */
package org.metaborg.sunshine.services.filesource;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class FileModifiedPair {

	private final long mod;
	private final FileObject f;

	public FileModifiedPair(FileObject file, long modified) {
		f = file;
		mod = modified;
	}

	public FileModifiedPair(FileObject file) {
		f = file;
		try {
			mod = file.getContent().getLastModifiedTime();
		} catch (FileSystemException e) {
			throw new RuntimeException(e);
		}
	}

	public FileObject getFile() {
		return f;
	}

	public long getModified() {
		return mod;
	}
}
