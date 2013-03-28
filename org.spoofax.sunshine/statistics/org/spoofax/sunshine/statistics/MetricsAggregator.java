/**
 * 
 */
package org.spoofax.sunshine.statistics;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.spoofax.sunshine.statistics.RoundMetrics.RoundType;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class MetricsAggregator {

	private RoundMetrics fullMetrics;
	private RoundMetrics incrMetrics;

	public void addMetrics(ProjectMetrics projMetrics, RoundMetrics fullMetrics, RoundMetrics incrementalMetrics) {
		assert projMetrics != null;
		assert fullMetrics.roundType == RoundType.FULL;
		assert incrementalMetrics.roundType == RoundType.INCREMENTAL;

		this.fullMetrics = fullMetrics;
		this.incrMetrics = incrementalMetrics;
		// TODO
	}

	public String getTimes() {
		StringBuilder buf = new StringBuilder();
		buf.append("============FULL TIMES==============\n");
		buf.append(getTimes(fullMetrics));
		buf.append("==========INCREMENTAL TIMES=================\n");
		buf.append(getTimes(incrMetrics));
		return buf.toString();
	}

	private StringBuilder getTimes(RoundMetrics incrMetrics2) {
		StringBuilder buf = new StringBuilder();
		Set<Entry<String, Double>> times = incrMetrics2.getRecordedTimes().entrySet();
		for (Entry<String, Double> time : times) {
			buf.append("\t");
			buf.append(time.getKey());
			buf.append(" => ");
			buf.append(time.getValue());
			buf.append("\n");
		}
		return buf;
	}

}
