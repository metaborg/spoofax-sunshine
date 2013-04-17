package org.spoofax.sunshine.gitdrive;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepository;
import org.eclipse.jgit.util.FS;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.library.IOperatorRegistry;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.sunshine.CompilerException;
import org.spoofax.sunshine.Environment;
import org.spoofax.sunshine.drivers.SunshineMainDriver;
import org.spoofax.sunshine.services.LanguageService;
import org.spoofax.sunshine.services.RuntimeService;
import org.spoofax.sunshine.statistics.BoxValidatable;
import org.spoofax.sunshine.statistics.Statistics;
import org.strategoxt.HybridInterpreter;

/**
 * 
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class SunshineGitDriver {

    private static final Logger logger = LogManager
	    .getLogger(SunshineGitDriver.class.getName());

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
		    RepositoryCache.FileKey.lenient(projectDir, FS.DETECTED),
		    true);
	    git = new Git(repo);
	} catch (IOException ioex) {
	    logger.fatal("Initialization failed with exception {}", ioex);
	    throw new RuntimeException("Git autopilot initialization failed",
		    ioex);
	}

	logger.trace("Selecting commits between {} and {}", args.fromCommit,
		args.toCommit);
	commits = GitUtils.getCommits(git, new FileExtensionRevFilter(
		LanguageService.INSTANCE().getSupportedExtens().iterator()
			.next(), git.getRepository()), true);
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
		actualCommits.add(cComm);
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
	    throw new RuntimeException("Last commit " + args.fromCommit
		    + " could not be found");
	}

	commits = actualCommits;

	logger.trace("Initialized git repository and found commit interval");
    }

    public void run() {
	int numCommits = commits.size();
	logger.debug("Going to iterate over {} commits", numCommits);
	RevCommit pCommit = null;
	for (RevCommit commit : commits) {
	    logger.info("Checking out commit {}/{} with hash {}",
		    currentCommitIndex++, numCommits, commit.getId().getName());
	    File savedCache = null;
	    if (!Environment.INSTANCE().getMainArguments().nonincremental)
		savedCache = saveCacheFolder();
	    GitUtils.cleanVeryHard(git);

	    GitUtils.stepRevision(git, pCommit, commit);
	    if (!Environment.INSTANCE().getMainArguments().nonincremental)
		restoreCacheFolder(savedCache);
	    else
		unloadIndex();

	    currentDriver = new SunshineMainDriver();
	    Statistics.addDataPoint("COMMIT", new BoxValidatable<String>(commit
		    .getId().getName()));
	    currentDriver.run();
	    pCommit = commit;
	}
	GitUtils.cleanVeryHard(git);
	// FIXME: checkout the master branch after we are done
    }

    private File saveCacheFolder() {
	logger.trace("Saving cache folder to a temporary directory");
	File cacheDir = Environment.INSTANCE().getCacheDir();
	File tempDir = new File(FileUtils.getTempDirectory(), "_sunshine_"
		+ System.currentTimeMillis());
	if (cacheDir.exists() && cacheDir.isDirectory()) {
	    try {
		FileUtils.moveDirectory(cacheDir, tempDir);
	    } catch (IOException ioex) {
		logger.fatal(
			"Could not move cache dir out of the way because of exception",
			ioex);
		throw new RuntimeException("Failed to move cache directory",
			ioex);
	    }
	    logger.trace("Moved cache dir {} to temporary {}", cacheDir,
		    tempDir);
	    return tempDir;
	} else {
	    logger.warn("Failed to save cacheDir {} because it does not exist",
		    cacheDir);
	    return null;
	}
    }

    private void restoreCacheFolder(File tmp) {
	File cacheDir = Environment.INSTANCE().getCacheDir();
	logger.trace("Restoring saved cache {} to original location {}", tmp,
		cacheDir);
	try {
	    if (cacheDir.exists())
		FileUtils.deleteDirectory(cacheDir);
	    FileUtils.moveDirectory(tmp, cacheDir);
	} catch (IOException ioex) {
	    logger.fatal(
		    "Could not move cache dir back in the project because of exception",
		    ioex);
	    throw new RuntimeException("Failed to restore saved cache dir",
		    ioex);
	}
    }

    protected void unloadIndex() {
	HybridInterpreter runtime = RuntimeService.INSTANCE().getRuntime(
		LanguageService.INSTANCE().getAnyLanguage());
	IOperatorRegistry idxLib = runtime.getContext().getOperatorRegistry(
		"INDEX");
	AbstractPrimitive unloadIdxPrim = idxLib.get("LANG_index_unload");
	try {
	    boolean unloadSuccess = unloadIdxPrim.call(
		    runtime.getContext(),
		    new Strategy[0],
		    new IStrategoTerm[] { runtime.getFactory()
			    .makeString(
				    Environment.INSTANCE().projectDir
					    .getAbsolutePath()) });
	    if (!unloadSuccess) {
		throw new CompilerException("Could not unload index");
	    }
	} catch (InterpreterException intex) {
	    throw new CompilerException("Could not unload index", intex);
	}

    }
}
