/**
 * 
 */
package org.spoofax.sunshine.services.files;

import org.spoofax.sunshine.util.IMerger;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class FileModifiedPairMerger implements IMerger<FileModifiedPair> {

    @Override
    public boolean areDifferent(FileModifiedPair older, FileModifiedPair newer) {
	if ((older == null || newer == null) && older != newer) {
	    return true;
	} else if (older == newer && older == null) {
	    return false;
	}

	return older.getModified() != newer.getModified();
//	return ((older == null || newer == null) && older != newer)
//		|| !(older.getFile().getPath()
//			.equals(newer.getFile().getPath())
//			&& older.getFile().isDirectory() == newer.getFile()
//				.isDirectory() && older.getModified() == newer
//			.getModified());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.spoofax.sunshine.util.IMerger#merge(java.lang.Object,
     * java.lang.Object)
     */
    @Override
    public FileModifiedPair merge(FileModifiedPair older, FileModifiedPair newer) {
	return areDifferent(older, newer) ? newer : older;
    }

}
