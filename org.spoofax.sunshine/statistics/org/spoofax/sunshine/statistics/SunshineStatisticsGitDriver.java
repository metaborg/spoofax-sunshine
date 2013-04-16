/**
 * 
 */
package org.spoofax.sunshine.statistics;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.gitective.core.CommitFinder;
import org.gitective.core.PathFilterUtils;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.sunshine.CompilerException;
import org.spoofax.sunshine.Environment;
import org.spoofax.sunshine.drivers.git.SunshineGitDriver;
import org.spoofax.sunshine.model.language.ALanguage;
import org.spoofax.sunshine.services.LanguageService;
import org.spoofax.sunshine.services.StrategoCallService;
import org.spoofax.sunshine.services.analyzer.AnalysisService;
import org.spoofax.sunshine.services.filesource.FileMonitoringService;
import org.spoofax.sunshine.statistics.model.BoxValidatable;
import org.spoofax.sunshine.statistics.model.DataRecording;
import org.spoofax.sunshine.statistics.model.IValidatable;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class SunshineStatisticsGitDriver extends SunshineGitDriver {

    public void step(java.util.Collection<java.io.File> files)
	    throws CompilerException {
	final RevCommit pRev = gitGetPreviousCommit();
	final RevCommit cRev = gitGetCurrentCommit();

	// compute project metrics & save them
	final ProjectMetrics projMetrics = getWebDSLMetrics();
	projMetrics.commit = cRev.getId().getName();
	projMetrics.commitDeltaLoc = gitComputeDeltaLoc(pRev, cRev);
	// final String commit = cRev.getId().getName();
	// final int commitDeltaLoc = gitComputeDeltaLoc(pRev, cRev);
	final File indexFile = new File(Environment.INSTANCE().projectDir,
		".cache/index.idx");
	assert indexFile.exists();
	try {
	    System.out.println("Preparing for full analysis");
	    // save index to safe location
	    final File incrementalIndexSaved = rescueIndex();

	    if (Environment.INSTANCE().getLaunchConfiguration().storeStats) {
		final DataRecording rec = RecordingStack.INSTANCE().next();
		rec.addDataPoint("ISFULL", IValidatable.ALWAYS_VALIDATABLE);
	    }

	    // reset everything
	    reset();
	    assert !indexFile.exists();

	    // perform full analysis
	    System.out.println("Doing full analysis.");
	    FileMonitoringService.INSTANCE().reset();
	    Collection<File> filesForFull = FileMonitoringService.INSTANCE()
		    .getChangesNoPersist();
	    if (Environment.INSTANCE().getLaunchConfiguration().storeStats) {
		final DataRecording rec = RecordingStack.INSTANCE().current();
		rec.addDataPoint("COMMIT", new BoxValidatable<String>(
			projMetrics.commit));
		rec.addDataPoint("LOC", new BoxValidatable<Integer>(
			projMetrics.loc));
		rec.addDataPoint("TDEF", new BoxValidatable<Integer>(
			projMetrics.tdefs));
		rec.addDataPoint("TCAL", new BoxValidatable<Integer>(
			projMetrics.tcalls));
		rec.addDataPoint("FILES", new BoxValidatable<Integer>(
			filesForFull.size()));

	    }
	    System.out.println("Analyzing " + filesForFull.size() + " files.");
	    AnalysisService.INSTANCE().analyze(filesForFull);
	    emitMessages();
	    assert indexFile.exists();
	    System.out.println("Full analysis completed.");

	    // ========

	    System.out.println("Preparing for incremental analysis");
	    if (Environment.INSTANCE().getLaunchConfiguration().storeStats) {
		final DataRecording rec = RecordingStack.INSTANCE().next();
		rec.addDataPoint("ISFULL", IValidatable.NEVER_VALIDATABLE);
	    }

	    // reset everything
	    reset();
	    assert !indexFile.exists();
	    // restore index
	    restoreIndex(incrementalIndexSaved);
	    assert indexFile.exists();
	    System.out.println("Analyzing " + files.size() + " files.");
	    if (Environment.INSTANCE().getLaunchConfiguration().storeStats) {
		final DataRecording rec = RecordingStack.INSTANCE().current();
		rec.addDataPoint("COMMIT", new BoxValidatable<String>(
			projMetrics.commit));
		rec.addDataPoint("LOC", new BoxValidatable<Integer>(
			projMetrics.loc));
		rec.addDataPoint("DELTALOC", new BoxValidatable<Integer>(
			projMetrics.commitDeltaLoc));
		rec.addDataPoint("TDEF", new BoxValidatable<Integer>(
			projMetrics.tdefs));
		rec.addDataPoint("TCAL", new BoxValidatable<Integer>(
			projMetrics.tcalls));
		rec.addDataPoint("FILES",
			new BoxValidatable<Integer>(files.size()));
	    }

	    // perform incremental analysis
	    AnalysisService.INSTANCE().analyze(files);
	    assert indexFile.exists();
	    System.out.println("Incremental analysis completed.");

	    // ====
	    // write statistics results
	    if (Environment.INSTANCE().getLaunchConfiguration().storeStats) {
		RecordingStack.INSTANCE().incrementalWriteToFile();
	    }
	} catch (IOException e) {
	    throw new CompilerException("Something broke", e);
	}

    }

    // hack
    private int previousDeltaCount = 0;

    public int gitComputeDeltaLoc(RevCommit parent, RevCommit current) {
	final CommitFinder finder = new CommitFinder(git.getRepository());
	final Set<String> extens = LanguageService.INSTANCE()
		.getSupportedExtens();
	final TreeFilter extensionFilter = PathFilterUtils.orSuffix(extens
		.toArray(new String[extens.size()]));
	finder.setFilter(extensionFilter);
	final LocDiffFilter diffCountFilter = new LocDiffFilter();
	finder.setMatcher(diffCountFilter);

	if (parent != null) {
	    finder.findBetween(parent.getId().getName(), current.getId()
		    .getName());
	} else {
	    finder.findUntil(current.getId().getName());
	}

	finder.find();
	int currentCount = diffCountFilter.count - previousDeltaCount;
	previousDeltaCount = diffCountFilter.count;
	return currentCount;
    }

    private ProjectMetrics getWebDSLMetrics() throws CompilerException {
	final ProjectMetrics metrics = new ProjectMetrics();
	final ALanguage webdsl = LanguageService.INSTANCE().getLanguageByName(
		"WebDSL");
	assert webdsl != null;
	final ITermFactory factory = Environment.INSTANCE().termFactory;
	final IStrategoTerm nil = factory.makeList();
	final IStrategoTerm inputTuple = factory.makeTuple(nil, nil, nil, nil,
		factory.makeString(Environment.INSTANCE().projectDir
			.getAbsolutePath()));
	assert inputTuple != null && inputTuple.getSubtermCount() == 5;
	final IStrategoTerm result = StrategoCallService.INSTANCE()
		.callStratego(webdsl, "webdsl-metrics", inputTuple);
	assert result.getTermType() == IStrategoTerm.TUPLE;
	assert result.getSubtermCount() == 2;
	final String metricString = ((IStrategoString) result.getSubterm(1))
		.stringValue();
	final String[] metricBits = metricString.split(",");
	assert metricBits.length == 3;
	metrics.loc = Integer.parseInt(metricBits[0]);
	metrics.tdefs = Integer.parseInt(metricBits[1]);
	metrics.tcalls = Integer.parseInt(metricBits[2]);
	return metrics;
    }

}
