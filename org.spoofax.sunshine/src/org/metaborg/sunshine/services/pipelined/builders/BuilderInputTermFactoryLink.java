/**
 * 
 */
package org.metaborg.sunshine.services.pipelined.builders;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metaborg.sunshine.environment.LaunchConfiguration;
import org.metaborg.sunshine.environment.ServiceRegistry;
import org.metaborg.sunshine.model.messages.IMessage;
import org.metaborg.sunshine.model.messages.MessageSeverity;
import org.metaborg.sunshine.pipeline.ILinkManyToOne;
import org.metaborg.sunshine.pipeline.ISinkOne;
import org.metaborg.sunshine.pipeline.diff.Diff;
import org.metaborg.sunshine.pipeline.diff.MultiDiff;
import org.metaborg.sunshine.services.analyzer.AnalysisFileResult;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class BuilderInputTermFactoryLink implements
		ILinkManyToOne<AnalysisFileResult, BuilderInputTerm> {

	private static final Logger logger = LogManager
			.getLogger(BuilderInputTermFactoryLink.class.getName());

	private final Collection<ISinkOne<BuilderInputTerm>> sinks = new HashSet<ISinkOne<BuilderInputTerm>>();

	private final File path;

	private boolean onSource;

	private boolean ignoreErrors;

	public BuilderInputTermFactoryLink(File file, boolean onSource,
			boolean ignoreErrors) {
		this.path = file;
		this.onSource = onSource;
		this.ignoreErrors = ignoreErrors;
	}

	@Override
	public void addSink(ISinkOne<BuilderInputTerm> sink) {
		assert sink != null;
		sinks.add(sink);
	}

	@Override
	public void sink(MultiDiff<AnalysisFileResult> product) {
		assert product != null;
		logger.trace("Creating builder input term for product");
		Diff<AnalysisFileResult> select = null;
		for (Diff<AnalysisFileResult> diff : product) {
			try {
				if (diff.getPayload().file().getCanonicalFile()
						.equals(path.getCanonicalFile())) {
					select = diff;
					break;
				} else {
					logger.trace(
							"Input file {} does not match prebaked file {}, skipping.",
							diff.getPayload().file(), path);
				}
			} catch (IOException ioex) {
				logger.error("File operations failed", ioex);
			}
		}
		if (select != null) {
			boolean errors_exist = false;
			for (IMessage msg : select.getPayload().messages()) {
				if (msg.severity() == MessageSeverity.ERROR) {
					errors_exist = true;
					break;
				}
			}
			if (!errors_exist || ignoreErrors) {
				logger.trace("Selected file {} for creating input", select
						.getPayload().file());

				IStrategoTerm ast = onSource
						&& select.getPayload().previousResult() != null ? select
						.getPayload().previousResult().ast()
						: select.getPayload().ast();
				LaunchConfiguration launch = ServiceRegistry.INSTANCE()
						.getService(LaunchConfiguration.class);
				BuilderInputTerm payload = new BuilderInputTerm(
						launch.termFactory, ast, select.getPayload().file(),
						launch.projectDir);
				Diff<BuilderInputTerm> result = new Diff<BuilderInputTerm>(
						payload, select.getDiffKind());
				for (ISinkOne<BuilderInputTerm> sink : sinks) {
					logger.trace(
							"Sinking input term for file {} to builder {}",
							path, sink);
					sink.sink(result);
				}
			} else {
				logger.info("Builder is skipping because of previous errors");
			}
		} else {
			logger.trace("No file in result matched the prebaked file {}", path);
		}
	}

}
