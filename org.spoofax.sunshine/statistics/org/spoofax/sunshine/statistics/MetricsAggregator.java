/**
 * 
 */
package org.spoofax.sunshine.statistics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.spoofax.sunshine.statistics.RoundMetrics.RoundType;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class MetricsAggregator {

	private File targetFile;
	private boolean hasWrittenHeader = false;

	public MetricsAggregator(File targetFile) {
		this.targetFile = targetFile;
		if (targetFile.exists()) {
			throw new RuntimeException("Refusing to overwrite old statistics. Delete the file manually");
		}
	}

	public void addMetrics(ProjectMetrics projMetrics, RoundMetrics fullMetrics, RoundMetrics incrementalMetrics) {
		assert projMetrics != null;
		assert fullMetrics.roundType == RoundType.FULL;
		assert incrementalMetrics.roundType == RoundType.INCREMENTAL;
		writeToFile(projMetrics, fullMetrics, incrementalMetrics);

	}

	private final static String C = ",";

	/*
	 * SEQNUM, COMMIT, FILES, LOC, TDEFS, TCALS, DELTA-LOC, DELTA-FILES, NUMTASKS-FULL, NUMTASKS-INCR,
	 * FULL-(*TIMES*), INCR-(*TIMES*)
	 */

	private void writeToFile(ProjectMetrics pM, RoundMetrics fM, RoundMetrics iM) {
		try {
			if (!targetFile.exists()) {
				targetFile.createNewFile();
			}
		} catch (IOException e) {
			throw new RuntimeException("Cannot create statistics file", e);
		}
		String header = "";
		String row = "";

		// statically defined things
		header = header + "SEQNUM, COMMIT, LOC, TDEFS, TCALS, DELTA-LOC, FULL-FILES, INCR-FILES, NUMTASKS-FULL, NUMTASKS-INCR ";
		row = row + pM.seqNum + C + pM.commit + C + pM.loc + C + pM.tdefs + C + pM.tcalls + C + pM.commitDeltaLoc + C;
		row = row + fM.analysisResults.keySet().size() + C + iM.analysisResults.keySet().size() + C;
		row = row + fM.tasks.getSubtermCount() + C + iM.tasks.getSubtermCount();

		ArrayList<Entry<String, Double>> fts = new ArrayList<Map.Entry<String, Double>>(fM.getRecordedTimes()
				.entrySet());
		Map<String, Double> its = iM.getRecordedTimes();
		Collections.sort(fts, new EntryComparator());

		for (Entry<String, Double> entry : fts) {
			header = header + ", FULL-" + entry.getKey() + ", INCR-" + entry.getKey();
			row = row + ", " + entry.getValue();
			Double it = its.get(entry.getKey());
			row = row + ", " + (it != null ? it : 0);
		}

		header = header + "\n";
		row = row + "\n";

		String toOut = !hasWrittenHeader ? header + row : row;
		hasWrittenHeader = true;
		try {
			FileUtils.writeStringToFile(targetFile, toOut, true);
		} catch (IOException e) {
			throw new RuntimeException("Cannot write statistics", e);
		}

	}

	private class EntryComparator implements Comparator<Entry<String, Double>> {

		@Override
		public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
			return o1.getKey().compareTo(o2.getKey());
		}

	}
}
