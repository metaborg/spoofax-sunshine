/**
 * 
 */
package org.spoofax.sunshine.statistics;

import java.util.Set;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.gitective.core.CommitFinder;
import org.gitective.core.CommitUtils;
import org.gitective.core.PathFilterUtils;
import org.spoofax.sunshine.CompilerException;
import org.spoofax.sunshine.LaunchConfiguration;
import org.spoofax.sunshine.drivers.git.SunshineGitDriver;
import org.spoofax.sunshine.framework.services.LanguageService;
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

		// save index to safe location
		// reset everything
		// perform full analysis
		// collect the results
		// reset everything

		// restore index
		// reset everything
		// perform incremental analysis
		// collect the results

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

}
