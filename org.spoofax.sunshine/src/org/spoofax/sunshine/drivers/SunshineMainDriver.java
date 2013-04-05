/**
 * 
 */
package org.spoofax.sunshine.drivers;

import java.io.File;
import java.util.Collection;
import java.util.Scanner;

import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.library.IOperatorRegistry;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.sunshine.CompilerCrashHandler;
import org.spoofax.sunshine.CompilerException;
import org.spoofax.sunshine.Environment;
import org.spoofax.sunshine.LaunchConfiguration;
import org.spoofax.sunshine.model.messages.IMessage;
import org.spoofax.sunshine.model.messages.MessageHelper;
import org.spoofax.sunshine.services.BuilderService;
import org.spoofax.sunshine.services.LanguageService;
import org.spoofax.sunshine.services.RuntimeService;
import org.spoofax.sunshine.services.analysis.AnalysisService;
import org.spoofax.sunshine.services.old.AnalysisResultsService;
import org.spoofax.sunshine.services.old.FileMonitoringService;
import org.spoofax.sunshine.services.old.MessageService;
import org.spoofax.sunshine.services.old.ParseService;
import org.strategoxt.HybridInterpreter;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class SunshineMainDriver {

	public SunshineMainDriver() {
		Thread.currentThread().setUncaughtExceptionHandler(new CompilerCrashHandler());
	}

	private void analyze(final Collection<File> files) {
		try {
			AnalysisService.INSTANCE().analyze(files);
		} catch (CompilerException e) {
			throw new RuntimeException("Analysis crashed", e);
		}
	}

	protected void emitMessages() {
		AnalysisResultsService.INSTANCE().commitMessages();
		final Collection<IMessage> msgs = MessageService.INSTANCE().getMessages();
		System.out.println("===============================");
		for (IMessage msg : msgs) {
			System.out.println(msg);
		}
		System.out.println("===============================");
	}

	public void init() throws CompilerException {
		final LaunchConfiguration config = Environment.INSTANCE().getLaunchConfiguration();
		LanguageService.INSTANCE().registerLanguage(config.languages);
		Environment.INSTANCE().setProjectDir(new File(config.project_dir));
		reset();
		warmup();
	}

	private void parse(final Collection<File> files) {
		for (File f : files) {
			ParseService.INSTANCE().parse(f);
		}
	}

	public void reset() throws CompilerException {
		new File(Environment.INSTANCE().projectDir, ".cache/index.idx").delete();
		try {
			unloadIndex();
		} catch (InterpreterException e) {
			throw new CompilerException(e);
		}
		MessageService.INSTANCE().clearMessages();
		AnalysisResultsService.INSTANCE().reset();
		System.gc();
	}

	protected void unloadIndex() throws InterpreterException {
		HybridInterpreter runtime = RuntimeService.INSTANCE().getRuntime(LanguageService.INSTANCE().getAnyLanguage());
		IOperatorRegistry idxLib = runtime.getContext().getOperatorRegistry("INDEX");
		AbstractPrimitive unloadIdxPrim = idxLib.get("LANG_index_unload");
		assert unloadIdxPrim.call(runtime.getContext(), new Strategy[0], new IStrategoTerm[] { runtime.getFactory()
				.makeString(Environment.INSTANCE().projectDir.getAbsolutePath()) });
	}

	public void run() throws CompilerException {
		init();
		Scanner sc = new Scanner(System.in);
		do {
			reset();
			Collection<File> files = FileMonitoringService.INSTANCE().getChanges();
			System.out.println("Changes: " + files);
			step(files);
		} while (Environment.INSTANCE().getLaunchConfiguration().as_daemon && sc.nextLine() != null);
	}

	public void step(Collection<File> files) throws CompilerException {
		final LaunchConfiguration config = Environment.INSTANCE().getLaunchConfiguration();
		final boolean statsEnabled = config.storeStats;
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

	private void warmup() throws CompilerException {
		final LaunchConfiguration config = Environment.INSTANCE().getLaunchConfiguration();
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

}
