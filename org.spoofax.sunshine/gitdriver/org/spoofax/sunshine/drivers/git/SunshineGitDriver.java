/**
 * 
 */
package org.spoofax.sunshine.drivers.git;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.management.RuntimeErrorException;

import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.util.FS;
import org.spoofax.sunshine.Environment;
import org.spoofax.sunshine.LaunchConfiguration;
import org.spoofax.sunshine.drivers.SunshineMainDriver;
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
		FileRepository repo;
		try {
			repo = (FileRepository) RepositoryCache.open(RepositoryCache.FileKey.lenient(projectDir, FS.DETECTED), true);
			git = new Git(repo);
		} catch (IOException e) {
			throw new RuntimeException("Git autopilot initialization failed", e);
		}
		
		// FileRepositoryBuilder gitBuilder = new FileRepositoryBuilder();
		// gitBuilder = gitBuilder.setGitDir(new File(projectDir,
		// ".git")).readEnvironment().findGitDir();
		// gitBuilder.setMustExist(true);
		// try {
		// git = new Git(gitBuilder.build());
		// } catch (IOException e) {
		// throw new RuntimeException("Could not find git repo", e);
		// }
	}

	@Override
	public void run() {
		init();
		List<RevCommit> commits = getCommits(true);
		System.out.println(commits.size());
		try {
			final RevCommit target = commits.iterator().next();
			System.out.println("Going back to commit: " + target.getId());
			CheckoutCommand cmd = git.checkout();
			cmd.setName("new-branch").setCreateBranch(true).setStartPoint(target).call();
			System.out.println(cmd.getResult());
			Runtime.getRuntime().exec("git clean -f -d", new String[0], git.getRepository().getDirectory());
		} catch (GitAPIException e) {
			throw new RuntimeException("Git autopilot crashed", e);
		} catch (IOException e) {
			throw new RuntimeException("Git autopilot crashed", e);
		}
	}

	private List<RevCommit> getCommits(boolean ascOrder) {
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
		List<RevCommit> commits = new LinkedList<RevCommit>();
		while (iter.hasNext()) {
			commits.add(iter.next());
		}
		if (ascOrder)
			Collections.reverse(commits);
		return commits;
	}

}
