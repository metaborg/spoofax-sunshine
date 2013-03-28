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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepository;
import org.eclipse.jgit.util.FS;
import org.eclipse.jgit.util.io.DisabledOutputStream;
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
	protected Git git;

	public SunshineGitDriver(LaunchConfiguration config) {
		super(config);
	}

	@Override
	public void init() throws CompilerException {
		super.init();
		final File projectDir = Environment.INSTANCE().projectDir;
		FileRepository repo;
		try {
			repo = (FileRepository) RepositoryCache
					.open(RepositoryCache.FileKey.lenient(projectDir, FS.DETECTED), true);
			git = new Git(repo);
			File indexFile = new File(projectDir, ".cache/index.idx");
			if (!indexFile.exists()) {
				if (!indexFile.getParentFile().exists())
					indexFile.getParentFile().mkdir();
				indexFile.createNewFile();
			}
		} catch (IOException e) {
			throw new RuntimeException("Git autopilot initialization failed", e);
		}
	}

	private RevCommit currentGitCommit;
	private RevCommit previousGitCommit;
	private int currentCommitSeqNum;

	public int getCurrentCommitSeqNum() {
		return currentCommitSeqNum;
	}

	protected RevCommit gitGetCurrentCommit() {
		return currentGitCommit;
	}

	protected RevCommit gitGetPreviousCommit() {
		return previousGitCommit;
	}

	@Override
	public void run() throws CompilerException {
		init();
		List<RevCommit> commits = getCommits(true);
		final int numCommits = commits.size();

		try {
			for (int idx = 0; idx < numCommits; idx++) {
				currentCommitSeqNum = idx;
				previousGitCommit = currentGitCommit;
				currentGitCommit = commits.get(idx);
				assert currentGitCommit != null;
				System.out.println("Checking out commit " + (idx + 1) + "/" + numCommits + " "
						+ currentGitCommit.getId());

				final File rescuedIndex = rescueIndex();

				stepRevision(previousGitCommit, currentGitCommit);
				restoreIndex(rescuedIndex);

				Collection<File> files = gitGetModifiedFiles(previousGitCommit, currentGitCommit);
				assert files != null;
				System.out.println("Processing: " + files);
				step(files);
			}
			git.checkout().setName("master").call();
			File indexRescued = rescueIndex();
			gitCleanVeryHard();
			restoreIndex(indexRescued);
			gitUpdateSubmodule();
			gitDeleteBranch(currentGitCommit.getName());
		} catch (GitAPIException e) {
			throw new RuntimeException("Git autopilot crashed", e);
		} catch (IOException e) {
			throw new RuntimeException("Git autopilot crashed", e);
		} catch (InterruptedException e) {
			throw new RuntimeException("Git autopilot crashed", e);
		}
	}

	protected File rescueIndex() throws IOException {
		File index = new File(Environment.INSTANCE().projectDir, ".cache/index.idx");
		if (index.exists()) {
			File tempFile = File.createTempFile("sunshine_index", null);
			tempFile.delete();
			FileUtils.moveFile(index, tempFile);
			System.out.println("Index rescued at " + tempFile.getAbsolutePath());
			return tempFile;
		} else {
			return null;
		}
	}

	protected void restoreIndex(File tempIndex) throws IOException {
		if (tempIndex == null)
			return;
		File cacheDir = new File(Environment.INSTANCE().projectDir, ".cache");
		if (!cacheDir.exists())
			cacheDir.mkdir();
		assert cacheDir.isDirectory() && cacheDir.exists();
		File index = new File(cacheDir, "index.idx");
		FileUtils.moveFile(tempIndex, index);
		System.out.println("Index restored at " + index.getAbsolutePath());
	}

	private void gitCleanVeryHard() throws InterruptedException, IOException {
		Process proc = Runtime.getRuntime().exec("git clean -f -f -d", new String[0],
				git.getRepository().getDirectory().getParentFile());
		proc.waitFor();
		assert proc.exitValue() == 0;
	}

	private void gitDeleteBranch(String branchname) throws GitAPIException {
		git.branchDelete().setBranchNames(branchname).setForce(true).call();
	}

	private void gitUpdateSubmodule() throws GitAPIException {
		git.submoduleUpdate().call();
	}

	private void stepRevision(RevCommit from, RevCommit to) throws GitAPIException, IOException, InterruptedException {
		git.checkout().setName(to.getName()).setCreateBranch(true).setStartPoint(to).call();
		try {
			gitUpdateSubmodule();
		} catch (RuntimeException gitex) {
			System.err.println("Failed to pull submodule for project. Currently at revision " + to.getId());
			gitex.printStackTrace();
		}
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
		rw.release();
		rw.dispose();
		return commits;
	}

	private Collection<File> gitGetModifiedFiles(RevCommit parent, RevCommit rev) throws IOException {
		final Collection<File> files = new LinkedList<File>();

		DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
		df.setRepository(git.getRepository());
		df.setDetectRenames(true);
		if (parent != null) {
			List<DiffEntry> diffs = df.scan(parent.getTree(), rev.getTree());
			for (DiffEntry diffEntry : diffs) {
				String affectedFN = diffEntry.getNewPath();
				if (hasSupportedExtension(affectedFN)) {
					files.add(new File(affectedFN));
				}
			}
		} else {
			files.addAll(FileMonitoringService.INSTANCE().getChangesNoPersist());
		}

		return files;
	}

	private boolean hasSupportedExtension(String filename) {
		final String exten = FilenameUtils.getExtension(filename);
		for (String supExt : LanguageService.INSTANCE().getSupportedExtens()) {
			if (exten.equals(supExt))
				return true;
		}
		return false;
	}

}
