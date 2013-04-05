/**
 * 
 */
package org.spoofax.sunshine.statistics;

import java.util.Collection;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.revwalk.RevCommit;
import org.gitective.core.filter.commit.DiffCountFilter;
import org.spoofax.sunshine.services.LanguageService;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class LocDiffFilter extends DiffCountFilter {

	public int count = 0;
	private final Set<String> extensions = LanguageService.INSTANCE().getSupportedExtens();

	@Override
	protected boolean include(RevCommit commit, DiffEntry diff, Collection<Edit> edits) {
		if (FilenameUtils.isExtension(diff.getNewPath(), extensions)) {
			for (Edit edit : edits)
				switch (edit.getType()) {
				case DELETE:
					count += edit.getLengthA();
					break;
				case INSERT:
				case REPLACE:
					count += edit.getLengthB();
					break;
				case EMPTY:
					break;
				default:
					break;
				}
		}
		return true;
	}

}
