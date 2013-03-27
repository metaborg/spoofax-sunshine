/**
 * 
 */
package org.spoofax.sunshine.statistics;

import org.spoofax.sunshine.CompilerException;
import org.spoofax.sunshine.LaunchConfiguration;
import org.spoofax.sunshine.drivers.git.SunshineGitDriver;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class SunshineStatisticsGitDriver extends SunshineGitDriver {

	public SunshineStatisticsGitDriver(LaunchConfiguration config) {
		super(config);
		assert config.storeStats;
		assert config.storeStatsAt != null;
	}

	public void step(java.util.Collection<java.io.File> files) throws CompilerException {
		// compute project metrics & save them
		// compute git commit statistics and save them
		
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
		
	};

}
