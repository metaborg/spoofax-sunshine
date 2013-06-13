package org.spoofax.sunshine.gitdrive;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepository;
import org.eclipse.jgit.util.FS;
import org.spoofax.sunshine.Environment;
import org.spoofax.sunshine.drivers.SunshineMainDriver;
import org.spoofax.sunshine.prims.ProjectUtils;
import org.spoofax.sunshine.services.LanguageService;
import org.spoofax.sunshine.statistics.BoxValidatable;
import org.spoofax.sunshine.statistics.Statistics;

/**
 * 
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class SunshineGitDriver {

	private static final Logger logger = LogManager.getLogger(SunshineGitDriver.class.getName());

	private SunshineGitArguments args;
	private Git git;
	private SunshineMainDriver currentDriver;

	private int currentCommitIndex;
	private List<RevCommit> commits;

	public SunshineGitDriver(SunshineGitArguments gitArgs) {
		this.args = gitArgs;
		logger.trace("New instance");
		initGit();
		assert git != null;
	}

	private void initGit() {
		logger.trace("Initializing git repository");
		final File projectDir = Environment.INSTANCE().projectDir;
		FileRepository repo;
		try {
			repo = (FileRepository) RepositoryCache.open(
					RepositoryCache.FileKey.lenient(projectDir, FS.DETECTED), true);
			git = new Git(repo);
		} catch (IOException ioex) {
			logger.fatal("Initialization failed with exception {}", ioex);
			throw new RuntimeException("Git autopilot initialization failed", ioex);
		}

		logger.trace("Selecting commits between {} and {}", args.fromCommit, args.toCommit);
		commits = GitUtils.getCommits(git, new FileExtensionRevFilter(LanguageService.INSTANCE()
				.getSupportedExtens().iterator().next(), git.getRepository()), true);
		boolean startCommitFound = args.fromCommit == null;
		boolean endCommitFound = false;
		Iterator<RevCommit> commitIter = commits.iterator();

		List<RevCommit> actualCommits = new LinkedList<RevCommit>();

		while (commitIter.hasNext()) {
			RevCommit cComm = commitIter.next();
			if (!startCommitFound) {
				if (cComm.getId().getName().equalsIgnoreCase(args.fromCommit)) {
					startCommitFound = true;
				}
			}
			if (startCommitFound && !endCommitFound) {
				logger.trace("Keeping commit {}", cComm.getName());
				if (!args.skipCommits.contains(cComm.getId().getName())) {
					actualCommits.add(cComm);
				} else {
					logger.debug("Skipping commit {}", cComm.getId().getName());
				}
				if (cComm.getId().getName().equalsIgnoreCase(args.toCommit)) {
					endCommitFound = true;
				}
			}
		}

		if (!startCommitFound) {
			logger.fatal("Could not find beginning commit {}", args.fromCommit);
			throw new RuntimeException("Beginning commit " + args.fromCommit
					+ " could not be found");
		}
		endCommitFound |= args.toCommit == null;

		if (!endCommitFound) {
			logger.fatal("Could not find last commit {}", args.fromCommit);
			throw new RuntimeException("Last commit " + args.fromCommit + " could not be found");
		}

		commits = actualCommits;

		logger.trace("Initialized git repository and found commit interval");
	}

	public void run() {
		int numCommits = commits.size();
		logger.debug("Going to iterate over {} commits", numCommits);
		RevCommit prevCommit = null;
		for (RevCommit commit : commits) {
			logger.info("Checking out commit {}/{} with hash {}", currentCommitIndex++, numCommits,
					commit.getId().getName());
			File savedCache = null;

			if (Environment.INSTANCE().getMainArguments().nonincremental) {
				ProjectUtils.cleanProject();
				ProjectUtils.unloadIndex();
				ProjectUtils.unloadTasks();
			} else {
				savedCache = ProjectUtils.saveProjectState();
			}

			GitUtils.cleanVeryHard(git);
			GitUtils.stepRevision(git, prevCommit, commit);

			if (savedCache != null) {
				ProjectUtils.restoreProjectState(savedCache);
			}

			currentDriver = new SunshineMainDriver();
			Statistics.addDataPoint("COMMIT", new BoxValidatable<String>(commit.getId().getName()));
			int deltaLoc = GitUtils.getDeltaLoc(git, prevCommit, commit, LanguageService.INSTANCE()
					.getAnyLanguage().getFileExtensions().iterator().next());
			Statistics.addDataPoint("DELTALOC", new BoxValidatable<Integer>(deltaLoc));
			currentDriver.run();
			prevCommit = commit;
		}
		GitUtils.cleanVeryHard(git);
		GitUtils.checkoutBranch(git, "master");
		GitUtils.updateSubmodule(git);
		GitUtils.deleteBranch(git, prevCommit.getName());
	}

}
