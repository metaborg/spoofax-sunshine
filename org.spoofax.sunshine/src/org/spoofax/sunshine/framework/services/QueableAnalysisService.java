/**
 * 
 */
package org.spoofax.sunshine.framework.services;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.spoofax.sunshine.CompilerException;
import org.spoofax.sunshine.framework.language.ALanguage;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
@Deprecated
public class QueableAnalysisService {

	private final AnalysisService runtime = AnalysisService.INSTANCE();

	private final BlockingQueue<QueueEntry> queue = new LinkedBlockingQueue<QueueEntry>();

	private static QueableAnalysisService INSTANCE;

	private QueableAnalysisService() {
	}

	public static QueableAnalysisService INSTANCE() {
		if (INSTANCE == null) {
			INSTANCE = new QueableAnalysisService();
		}
		return INSTANCE;
	}

	public void enqueueAnalysis(Collection<File> files, String function) {
		queue.add(new QueueEntry(files, function));
	}

	public void enqueueAnalysis(Collection<File> files) {
		queue.add(new QueueEntry(files, null));
	}

	public void analyzeQueue() throws CompilerException {
		while (queue.peek() != null) {
			try {
				final QueueEntry unit = queue.take();
				final Set<ALanguage> languages = new HashSet<ALanguage>();
				for (File f : unit.files) {
					languages.add(LanguageService.INSTANCE().getLanguageByExten(f));
				}
				// temporarily set the analysis function
				if (unit.function != null)
					for (ALanguage lang : languages) {
						lang.overrideAnalysisFunction(unit.function);
					}
				runtime.analyze(unit.files);
				// restore the analysis function
				if (unit.function != null)
					for (ALanguage lang : languages) {
						lang.restoreAnalysisFunction();
					}
			} catch (InterruptedException e) {
				;
			}
		}
	}

	private class QueueEntry {
		public QueueEntry(Collection<File> files, String function) {
			this.files = files;
			this.function = function;
		}

		public String function;
		public Collection<File> files;
	}
}
