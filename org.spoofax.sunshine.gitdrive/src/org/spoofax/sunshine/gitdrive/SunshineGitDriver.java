package org.spoofax.sunshine.gitdrive;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepository;
import org.eclipse.jgit.util.FS;
import org.spoofax.sunshine.Environment;
import org.spoofax.sunshine.drivers.SunshineMainDriver;
import org.spoofax.sunshine.services.LanguageService;

/**
 * 
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class SunshineGitDriver {

    private static final Logger logger = LogManager
	    .getLogger(SunshineGitDriver.class.getName());

    private GitRunArguments args;
    private Git git;
    private SunshineMainDriver currentDriver;

    private int currentCommitIndex;
    private List<RevCommit> commits;

    public SunshineGitDriver(GitRunArguments gitArgs) {
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
		    RepositoryCache.FileKey.lenient(projectDir, FS.DETECTED),
		    true);
	    git = new Git(repo);
	    // File indexFile = new File(Environment.INSTANCE().getCacheDir(),
	    // "index.idx");
	} catch (IOException ioex) {
	    logger.fatal("Initialization failed with exception {}", ioex);
	    throw new RuntimeException("Git autopilot initialization failed",
		    ioex);
	}

	logger.trace("Selecting commits between {} and {}", args.fromCommit,
		args.toCommit);
	commits = getCommits(true);
	boolean startCommitFound = args.fromCommit == null;
	boolean endCommitFound = false;
	Iterator<RevCommit> commitIter = commits.iterator();

	List<RevCommit> actualCommits = new LinkedList<RevCommit>();

	while (commitIter.hasNext()) {
	    RevCommit cComm = commitIter.next();
	    if (!startCommitFound) {
		if (cComm.getId().getName().equalsIgnoreCase(args.fromCommit)) {
		    startCommitFound = true;
		} else {
		    logger.trace(
			    "Disregarding commit {} because it's before beginning",
			    cComm.getName());
		}
	    }
	    if (startCommitFound && !endCommitFound) {
		logger.trace("Keeping commit {}", cComm.getName());
		actualCommits.add(cComm);
		if (cComm.getId().getName().equalsIgnoreCase(args.toCommit)) {
		    endCommitFound = true;
		}
	    } else {
		logger.trace("Disregarding commit {} because it's after end",
			cComm.getName());
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
	    throw new RuntimeException("Last commit " + args.fromCommit
		    + " could not be found");
	}

	commits = actualCommits;

	logger.trace("Initialized git repository and found commit interval");
    }

    private void resetState() {
	logger.debug("Resetting state");
	// delete cache folder
	// unload index
	// reload index
	// create new SunshineMainDriver
	throw new RuntimeException("NOt implemented");
    }

    public void run() {
	int numCommits = commits.size();
	logger.debug("Going to iterate over {} commits", numCommits);
	// TODO Auto-generated method stub
    }

    private List<RevCommit> getCommits(boolean ascOrder) {
	final Repository repo = git.getRepository();
	final RevWalk rw = new RevWalk(repo);

	try {
	    rw.markStart(rw.parseCommit(repo.resolve(Constants.HEAD)));
	} catch (Exception ex) {
	    throw new RuntimeException("Failed to initialize revwalker", ex);
	}
	rw.setRevFilter(new FileExtensionRevFilter(LanguageService.INSTANCE()
		.getSupportedExtens().iterator().next(), git.getRepository()));
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

}
