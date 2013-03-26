/**
 * 
 */
package org.spoofax.sunshine.drivers.git;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.spoofax.sunshine.Environment;
import org.spoofax.sunshine.LaunchConfiguration;
import org.spoofax.sunshine.SunshineMainDriver;
import org.spoofax.sunshine.framework.services.LanguageService;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class SunshineGitDriver extends SunshineMainDriver {
	private Git git;

	public SunshineGitDriver(LaunchConfiguration config) {
		super(config);
	}

	@Override
	public void init() {
		super.init();
		final File projectDir = Environment.INSTANCE().projectDir;
		FileRepositoryBuilder gitBuilder = new FileRepositoryBuilder();
		gitBuilder = gitBuilder.setGitDir(new File(projectDir, ".git")).readEnvironment().findGitDir();
		gitBuilder.setMustExist(true);
		try {
			git = new Git(gitBuilder.build());
		} catch (IOException e) {
			throw new RuntimeException("Could not find git repo", e);
		}
	}

	@Override
	public void run() {
		init();
		RevWalk rw = new RevWalk(git.getRepository());
		try {
			rw.markStart(rw.parseCommit(git.getRepository().resolve(Constants.HEAD)));
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}

		rw.setRevFilter(new FileExtensionRevFilter(LanguageService.INSTANCE().getSupportedExtens().iterator().next(),
				git.getRepository()));
		Iterator<RevCommit> iter = rw.iterator();
		Collection<RevCommit> commits = new LinkedList<RevCommit>();
		while (iter.hasNext()) {
			RevCommit rev = iter.next();
			// System.out.println("Working on commit " + rev + " msg " + rev.getShortMessage());
			commits.add(rev);
		}
		System.out.println("Total commits: " + commits.size());
	}

	private List<RevCommit> getCommits(boolean naturalOrder) {
		final Repository repo = git.getRepository();
		final RevWalk rw = new RevWalk(repo);

		try {
			rw.markStart(rw.parseCommit(repo.resolve(Constants.HEAD)));
		} catch (Exception ex) {
			throw new RuntimeException("Failed to initialize revwalker", ex);
		}
		rw.setRevFilter(new FileExtensionRevFilter(LanguageService.INSTANCE().getSupportedExtens().iterator().next(),
				git.getRepository()));
		Iterator<RevCommit> iter = rw.iterator();
		Collection<RevCommit> commits = new LinkedList<RevCommit>();
		
		while (iter.hasNext() && commits.add(iter.next()));

		// TODO
		return null;
	}

}
