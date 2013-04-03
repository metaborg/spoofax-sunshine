/**
 * 
 */
package org.spoofax.sunshine.statistics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.io.FileUtils;
import org.spoofax.sunshine.Environment;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class RecordingStack {

	private static RecordingStack INSTANCE;

	private RecordingStack() {
	}

	public static RecordingStack INSTANCE() {
		if (INSTANCE == null) {
			INSTANCE = new RecordingStack();
		}
		return INSTANCE;
	}

	private final Stack<DataRecording> recordingStack = new Stack<DataRecording>();
	private final File targetFile = Environment.INSTANCE().getLaunchConfiguration().storeStatsAt;

	public void reset() {
		assert recordingStack != null;
		recordingStack.clear();
		headerWritten = false;
	}

	public DataRecording next() {
		if (recordingStack.size() > 0) {
			recordingStack.peek().close();
		}
		final DataRecording recording = new DataRecording();
		recordingStack.push(recording);
		return recording;
	}

	public DataRecording current() {
		return recordingStack.peek();
	}

	private static final String NUM_FORMAT_STRING = "%D";
	private static final String HEAD_FORMAT_STRING = "%S";
	private static final String NEWL_FORMAT_STRING = "%n";
	private static final String SEQNUM_HEAD = "SEQNUM";
	private static final String COMMA_STRING = ",";

	private static final String DEFAULT_VALUE = "-1";

	private Tuple2<String, String> toCSV() {
		final Enumeration<DataRecording> recordingsEnum = recordingStack.elements();
		final Set<String> keySet = new HashSet<String>(recordingStack.size());
		while (recordingsEnum.hasMoreElements()) {
			keySet.addAll(recordingsEnum.nextElement().getKeys());
		}
		final List<String> keys = new ArrayList<String>(keySet);
		Collections.sort(keys);

		final StringBuilder header = new StringBuilder();
		final Formatter headerFormatter = new Formatter(header, Locale.US);
		headerFormatter.format(makeFormatString(keys, HEAD_FORMAT_STRING), SEQNUM_HEAD, keys.toArray());
		headerFormatter.close();

		final StringBuilder row = new StringBuilder();
		final Formatter rowFormatter = new Formatter(row, Locale.US);
		final String rowFormat = makeFormatString(keys, NUM_FORMAT_STRING);
		for (int idx = 0; idx < recordingStack.size(); idx++) {
			rowFormatter.format(rowFormat, idx, recordingStack.pop().toOrderedStrings(keys, DEFAULT_VALUE).toArray());
		}
		rowFormatter.close();

		return new Tuple2<String, String>(header.toString(), row.toString());
	}

	private String makeFormatString(List<String> keys, String entryType) {
		final StringBuilder headerFormat = new StringBuilder();
		headerFormat.append(entryType);
		for (int idx = 0; idx < keys.size(); idx++) {
			headerFormat.append(COMMA_STRING);
			headerFormat.append(entryType);
		}
		headerFormat.append(NEWL_FORMAT_STRING);
		return headerFormat.toString();
	}

	private boolean headerWritten;

	public void incrementalWriteToFile() throws IOException {
		if (!headerWritten) {
			if (!targetFile.exists()) {
				if (!targetFile.getParentFile().exists()) {
					targetFile.getParentFile().mkdir();
				}
				targetFile.createNewFile();
			}
		}
		assert targetFile.exists();

		final Tuple2<String, String> csv = toCSV();
		recordingStack.clear();
		if (!headerWritten) {
			FileUtils.writeStringToFile(targetFile, csv._1);
			headerWritten = true;
		}
		FileUtils.writeStringToFile(targetFile, csv._2, true);
	}

	private class Tuple2<L, R> {
		public L _1;
		public R _2;

		public Tuple2(L l, R r) {
			this._1 = l;
			this._2 = r;
		}
	}

}
