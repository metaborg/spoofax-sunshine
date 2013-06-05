/**
 * 
 */
package org.spoofax.sunshine.services.pipelined.builders;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.sunshine.Environment;
import org.spoofax.sunshine.parser.model.IStrategoParseOrAnalyzeResult;
import org.spoofax.sunshine.pipeline.ILinkManyToOne;
import org.spoofax.sunshine.pipeline.ISinkOne;
import org.spoofax.sunshine.pipeline.diff.Diff;
import org.spoofax.sunshine.pipeline.diff.MultiDiff;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class BuilderInputTermFactoryLink implements
		ILinkManyToOne<IStrategoParseOrAnalyzeResult, BuilderInputTerm> {

	private static final Logger logger = LogManager.getLogger(BuilderInputTermFactoryLink.class
			.getName());

	private final Collection<ISinkOne<BuilderInputTerm>> sinks = new HashSet<ISinkOne<BuilderInputTerm>>();

	private final File path;

	private boolean onSource;

	public BuilderInputTermFactoryLink(File file, boolean onSource) {
		this.path = file;
		this.onSource = onSource;
	}

	@Override
	public void addSink(ISinkOne<BuilderInputTerm> sink) {
		assert sink != null;
		sinks.add(sink);
	}

	@Override
	public void sink(MultiDiff<IStrategoParseOrAnalyzeResult> product) {
		assert product != null;
		logger.trace("Creating builder input term for product");
		Diff<IStrategoParseOrAnalyzeResult> select = null;
		for (Diff<IStrategoParseOrAnalyzeResult> diff : product) {
			try {
				if (diff.getPayload().file().getCanonicalFile().equals(path.getCanonicalFile())) {
					select = diff;
					break;
				} else {
					logger.trace("Input file {} does not match prebaked file {}, skipping.", diff
							.getPayload().file(), path);
				}
			} catch (IOException ioex) {
				logger.error("File operations failed", ioex);
			}
		}
		if (select != null) {
			logger.trace("Selected file {} for creating input", select.getPayload().file());
			IStrategoTerm ast = onSource && select.getPayload().previousAst() != null ? select
					.getPayload().previousAst() : select.getPayload().ast();
			BuilderInputTerm payload = new BuilderInputTerm(Environment.INSTANCE().termFactory,
					ast, select.getPayload().file(), Environment.INSTANCE().projectDir);
			Diff<BuilderInputTerm> result = new Diff<BuilderInputTerm>(payload,
					select.getDiffKind());
			for (ISinkOne<BuilderInputTerm> sink : sinks) {
				logger.trace("Sinking input term for file {} to builder {}", path, sink);
				sink.sink(result);
			}
		} else {
			logger.trace("No file in result matched the prebaked file {}", path);
		}
	}

}
