/**
 * 
 */
package org.spoofax.sunshine.drivers;

import java.io.File;
import java.util.Collection;
import java.util.Scanner;

import org.spoofax.sunshine.CompilerCrashHandler;
import org.spoofax.sunshine.CompilerException;
import org.spoofax.sunshine.Environment;
import org.spoofax.sunshine.LaunchConfiguration;
import org.spoofax.sunshine.framework.messages.IMessage;
import org.spoofax.sunshine.framework.messages.MessageHelper;
import org.spoofax.sunshine.framework.services.AnalysisResultsService;
import org.spoofax.sunshine.framework.services.AnalysisService;
import org.spoofax.sunshine.framework.services.BuilderService;
import org.spoofax.sunshine.framework.services.FileMonitoringService;
import org.spoofax.sunshine.framework.services.LanguageService;
import org.spoofax.sunshine.framework.services.MessageService;
import org.spoofax.sunshine.framework.services.ParseService;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class SunshineMainDriver {
	private LaunchConfiguration config;

	public SunshineMainDriver(LaunchConfiguration config) {
		Thread.currentThread().setUncaughtExceptionHandler(new CompilerCrashHandler());
		this.config = config;
		System.out.println("Configuration: \n" + config);
	}

	public void init() {
		LanguageService.INSTANCE().registerLanguage(config.languages);
		Environment.INSTANCE().setProjectDir(new File(config.project_dir));
		warmup();
	}

	public void step(Collection<File> files) throws CompilerException {
		CompilerException crashCause = null;
		try {
			if (config.doParseOnly) {
				parse(files);
			} else {
				if (files.size() > 0) {
					boolean success = !config.doPreAnalysisBuild;
					if (config.doPreAnalysisBuild) {
						success = BuilderService.INSTANCE().callBuilder(config.builderTarget,
								config.preAnalysisBuilder, true) != null;
					}
					if (success && config.doAnalyze) {
						analyze(files);
					} else {
						MessageService.INSTANCE().addMessage(
								MessageHelper.newAnalysisErrorAtTop(files.iterator().next().getPath(),
										"Analysis failed. Dependency failed."));
					}

					if (success && config.doPostAnalysisBuild) {
						success = BuilderService.INSTANCE().callBuilder(config.builderTarget,
								config.postAnalysisBuilder, false) != null;
					}

					if (!success) {
						MessageService.INSTANCE().addMessage(
								MessageHelper
										.newBuilderErrorAtTop(files.iterator().next().getPath(), "Builder failed."));
					}
				}
			}
		} catch (CompilerException cex) {
			crashCause = cex;
		}
		emitMessages();
		if (crashCause != null) {
			throw crashCause;
		}
	}

	public void run() throws CompilerException {
		init();
		Scanner sc = new Scanner(System.in);
		do {
			reset();
			Collection<File> files = FileMonitoringService.INSTANCE().getChanges();
			System.out.println("Changes: " + files);
			step(files);
		} while (config.as_daemon && sc.nextLine() != null);
	}

	private void emitMessages() {
		AnalysisResultsService.INSTANCE().commitMessages();
		final Collection<IMessage> msgs = MessageService.INSTANCE().getMessages();
		System.out.println("===============================");
		for (IMessage msg : msgs) {
			System.out.println(msg);
		}
		System.out.println("===============================");
	}

	private void reset() {
		new File(Environment.INSTANCE().projectDir, ".cache/index.idx").delete();
		MessageService.INSTANCE().clearMessages();
		AnalysisResultsService.INSTANCE().reset();
		System.gc();
		// TODO: reset index cache
	}

	private void warmup() {
		System.out.println("Warming up " + config.warmup_rounds + " rounds.");
		long begin = 0;
		long end = 0;
		for (int i = config.warmup_rounds; i > 0; i--) {
			begin = System.currentTimeMillis();
			final Collection<File> files = FileMonitoringService.INSTANCE().getChangesNoPersist();
			if (config.doParseOnly) {
				parse(files);
			} else {
				analyze(files);
			}
			end = System.currentTimeMillis();
			System.out.println("Round " + (config.warmup_rounds - i + 1) + " done in " + (end - begin) + " ms");
			reset();
		}

		MessageService.INSTANCE().clearMessages();
		System.out.println("Warm up completed. Last duration: " + (end - begin) + " ms");
	}

	private void parse(final Collection<File> files) {
		for (File f : files) {
			ParseService.INSTANCE().parse(f);
		}
	}

	private void analyze(final Collection<File> files) {
		try {
			AnalysisService.INSTANCE().analyze(files);
		} catch (CompilerException e) {
			throw new RuntimeException("Analysis crashed", e);
		}
	}

}
