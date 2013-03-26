/**
 * 
 */
package org.spoofax.sunshine.drivers.git;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepository;
import org.eclipse.jgit.util.FS;
import org.spoofax.sunshine.CompilerException;
import org.spoofax.sunshine.Environment;
import org.spoofax.sunshine.LaunchConfiguration;
import org.spoofax.sunshine.drivers.SunshineMainDriver;
import org.spoofax.sunshine.framework.services.FileMonitoringService;
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
			repo = (FileRepository) RepositoryCache
					.open(RepositoryCache.FileKey.lenient(projectDir, FS.DETECTED), true);
			git = new Git(repo);
		} catch (IOException e) {
			throw new RuntimeException("Git autopilot initialization failed", e);
		}
	}

	@Override
	public void run() throws CompilerException {
		init();
		List<RevCommit> commits = getCommits(true);
		final int numCommits = commits.size();
		RevCommit previous = null;
		RevCommit current = null;
		try {
			for (int idx = 0; idx < numCommits; idx++) {
				previous = current;
				current = commits.get(idx);
				assert current != null;
				System.out.println("Checking out commit " + (idx + 1) + "/" + numCommits);
				stepRevision(previous, current);
				final Collection<File> files = FileMonitoringService.INSTANCE().getChanges();
				System.out.println("Processing: " + files);
				step(files);
				FileMonitoringService.INSTANCE().reset();
				if (idx >= 50)
					break;
			}
			git.checkout().setName("master").call();
			gitCleanVeryHard();
			gitUpdateSubmodule();
			gitDeleteBranch(current.getName());
		} catch (GitAPIException e) {
			throw new RuntimeException("Git autopilot crashed", e);
		} catch (IOException e) {
			throw new RuntimeException("Git autopilot crashed", e);
		} catch (InterruptedException e) {
			throw new RuntimeException("Git autopilot crashed", e);
		}
	}

	private void gitCleanVeryHard() throws InterruptedException, IOException {
		Process proc = Runtime.getRuntime().exec("git clean -f -f -d", new String[0], git.getRepository().getDirectory().getParentFile());
		proc.waitFor();
		assert proc.exitValue() == 0;
	}

	private void gitDeleteBranch(String branchname) throws GitAPIException {
		git.branchDelete().setBranchNames(branchname).call();
	}
	
	private void gitUpdateSubmodule() throws GitAPIException {
		git.submoduleUpdate().call();
	}

	private void stepRevision(RevCommit from, RevCommit to) throws GitAPIException, IOException, InterruptedException {
		git.checkout().setName(to.getName()).setCreateBranch(true).setStartPoint(to).call();
		gitUpdateSubmodule();
		gitCleanVeryHard();
		if (from != null) {
			gitDeleteBranch(from.getName());
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
		List<RevCommit> commits = new ArrayList<RevCommit>();
		while (iter.hasNext()) {
			commits.add(iter.next());
		}
		if (ascOrder)
			Collections.reverse(commits);
		rw.dispose();
		return commits;
	}
	
	private Collection<File> gitGetModifiedFiles(RevCommit rev){
		final Collection<File> files = new LinkedList<File>();
		// TODO

		return files;
	}

}
