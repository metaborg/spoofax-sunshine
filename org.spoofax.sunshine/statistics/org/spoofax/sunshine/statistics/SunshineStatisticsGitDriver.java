/**
 * 
 */
package org.spoofax.sunshine.statistics;

import java.io.File;
import java.io.IOException;
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
import org.spoofax.sunshine.LaunchConfiguration;
import org.spoofax.sunshine.drivers.git.SunshineGitDriver;
import org.spoofax.sunshine.framework.language.ALanguage;
import org.spoofax.sunshine.framework.services.AnalysisResultsService;
import org.spoofax.sunshine.framework.services.AnalysisService;
import org.spoofax.sunshine.framework.services.FileMonitoringService;
import org.spoofax.sunshine.framework.services.LanguageService;
import org.spoofax.sunshine.framework.services.StrategoCallService;
import org.spoofax.sunshine.statistics.RoundMetrics.RoundType;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class SunshineStatisticsGitDriver extends SunshineGitDriver {

	private final MetricsAggregator aggregator;

	public SunshineStatisticsGitDriver(LaunchConfiguration config) {
		super(config);
		assert config.storeStats;
		assert config.storeStatsAt != null;
		if (!config.storeStatsAt.exists())
			config.storeStatsAt.mkdir();
		this.aggregator = new MetricsAggregator();
	}

	public void step(java.util.Collection<java.io.File> files) throws CompilerException {
		final RoundMetrics fullMetrics = new RoundMetrics(RoundType.FULL);
		final RoundMetrics incrMetrics = new RoundMetrics(RoundType.INCREMENTAL);
		final RevCommit pRev = gitGetPreviousCommit();
		final RevCommit cRev = gitGetCurrentCommit();

		// compute git commit statistics and save them
		int deltaLoc = gitComputeDeltaLoc(pRev, cRev);
		fullMetrics.commitDeltaLines = deltaLoc;
		incrMetrics.commitDeltaLines = deltaLoc;

		// compute project metrics & save them
		final ProjectMetrics projMetrics = getWebDSLMetrics();

		try {
			// save index to safe location
			final File incrementalIndexSaved = rescueIndex();
			
			Environment.INSTANCE().setCurrentRoundMetric(fullMetrics);
			
			// reset everything
			reset();
			// TODO reload index

			// perform full analysis
			FileMonitoringService.INSTANCE().reset();
			AnalysisService.INSTANCE().analyze(FileMonitoringService.INSTANCE().getChangesNoPersist());
			// collect the results
			fullMetrics.analysisResults.putAll(AnalysisResultsService.INSTANCE().getAllResultsMap());
			
			// collect & store the times taken
			// TODO
			
			Environment.INSTANCE().setCurrentRoundMetric(incrMetrics);
			
			// reset everything
			reset();
			
			// restore index
			restoreIndex(incrementalIndexSaved);
			// reset everything
			reset();
			// TODO reload index
			
			// perform incremental analysis
			AnalysisService.INSTANCE().analyze(files);
			// collect the results
			incrMetrics.analysisResults.putAll(AnalysisResultsService.INSTANCE().getAllResultsMap());
			
			//collect and store the times taken
			// TODO
			
			aggregator.addMetrics(projMetrics, fullMetrics, incrMetrics);
		} catch (IOException e) {
			throw new CompilerException("Something broke", e);
		}

		// add the results to the statistics aggregator for evaluation & compression

	}

	// hack
	private int previousDeltaCount = 0;

	public int gitComputeDeltaLoc(RevCommit parent, RevCommit current) {
		final CommitFinder finder = new CommitFinder(git.getRepository());
		final Set<String> extens = LanguageService.INSTANCE().getSupportedExtens();
		final TreeFilter extensionFilter = PathFilterUtils.orSuffix(extens.toArray(new String[extens.size()]));
		finder.setFilter(extensionFilter);
		final LocDiffFilter diffCountFilter = new LocDiffFilter();
		finder.setMatcher(diffCountFilter);

		if (parent != null) {
			finder.findBetween(parent.getId().getName(), current.getId().getName());
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
		final ALanguage webdsl = LanguageService.INSTANCE().getLanguageByName("WebDSL");
		assert webdsl != null;
		final ITermFactory factory = Environment.INSTANCE().termFactory;
		final IStrategoTerm nil = factory.makeList();
		final IStrategoTerm inputTuple = factory.makeTuple(nil, nil, nil, nil,
				factory.makeString(Environment.INSTANCE().projectDir.getAbsolutePath()));
		assert inputTuple != null && inputTuple.getSubtermCount() == 5;
		final IStrategoTerm result = StrategoCallService.INSTANCE().callStratego(webdsl, "webdsl-metrics", inputTuple);
		assert result.getTermType() == IStrategoTerm.TUPLE;
		assert result.getSubtermCount() == 2;
		final String metricString = ((IStrategoString) result.getSubterm(1)).stringValue();
		final String[] metricBits = metricString.split(",");
		assert metricBits.length == 3;
		metrics.loc = Integer.parseInt(metricBits[0]);
		metrics.tdefs = Integer.parseInt(metricBits[1]);
		metrics.tcalls = Integer.parseInt(metricBits[2]);
		return metrics;
	}

}
