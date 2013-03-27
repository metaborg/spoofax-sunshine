/**
 * 
 */
package org.spoofax.sunshine.statistics;

import org.spoofax.sunshine.statistics.RoundMetrics.RoundType;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 *
 */
public class MetricsAggregator {

	
	public void addMetrics(ProjectMetrics projMetrics, RoundMetrics fullMetrics, RoundMetrics incrementalMetrics){
		assert projMetrics != null;
		assert fullMetrics.roundType == RoundType.FULL;
		assert incrementalMetrics.roundType == RoundType.INCREMENTAL;
		
		// TODO
	}
	
}
