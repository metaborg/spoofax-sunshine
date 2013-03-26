/**
 * 
 */
package org.spoofax.sunshine.drivers.git;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.StopWalkException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.util.io.DisabledOutputStream;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class FileExtensionRevFilter extends RevFilter {

	private final String exten;
	private final Repository repo;

	public FileExtensionRevFilter(String exten, Repository repo) {
		assert exten != null && exten.length() > 0;
		assert repo != null;
		this.exten = exten;
		this.repo = repo;
	}

	@Override
	public RevFilter clone() {
		return new FileExtensionRevFilter(exten, repo);
	}

	@Override
	public boolean include(RevWalk rw, RevCommit rev) throws StopWalkException, MissingObjectException,
			IncorrectObjectTypeException, IOException {
		DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
		df.setRepository(repo);
		df.setDiffComparator(RawTextComparator.DEFAULT);
		df.setDetectRenames(true);
		if (rev.getParentCount() > 0) {
			RevCommit parentRev = rw.parseCommit(rev.getParent(0).getId());
			List<DiffEntry> diffs = df.scan(parentRev.getTree(), rev.getTree());
			for (DiffEntry diffEntry : diffs) {
				if (FilenameUtils.getExtension(diffEntry.getNewPath()).equals(exten)) {
					return true;
				}
			}
		} else {
			return true;
		}

		return true;
	}
}
