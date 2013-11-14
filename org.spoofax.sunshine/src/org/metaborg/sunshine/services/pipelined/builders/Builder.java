package org.metaborg.sunshine.services.pipelined.builders;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metaborg.sunshine.CompilerException;
import org.metaborg.sunshine.Environment;
import org.metaborg.sunshine.services.StrategoCallService;
import org.metaborg.sunshine.services.language.ALanguage;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * An implementation of the {@link IBuilder} interface. This implementation differentiates the
 * required post-processing steps (writing or not to file) based on the output of the strategy
 * invocation.
 * 
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class Builder implements IBuilder {

	private static final Logger logger = LogManager.getLogger(Builder.class.getName());

	private final String name;
	private final boolean onSource;
	private final boolean meta;
	private final String strategy;
	private final ALanguage language;

	public Builder(String name, String strategy, ALanguage language, boolean onSource, boolean meta) {
		this.name = name;
		this.onSource = onSource;
		this.meta = meta;
		this.strategy = strategy;
		this.language = language;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isOnSource() {
		return onSource;
	}

	@Override
	public boolean isMeta() {
		return meta;
	}

	@Override
	public String getInvocationTarget() {
		return strategy;
	}

	@Override
	public ALanguage getLanguage() {
		return language;
	}

	@Override
	public IStrategoTerm invoke(IStrategoTerm input) {
		IStrategoTerm result = StrategoCallService.INSTANCE().callStratego(getLanguage(),
				getInvocationTarget(), input);
		processResult(result);
		return result;
	}

	private void processResult(IStrategoTerm result) {
		if (isWriteFile(result)) {

			final File resultFile = new File(Environment.INSTANCE().projectDir,
					((IStrategoString) result.getSubterm(0)).stringValue());
			final String resultContents = ((IStrategoString) result.getSubterm(1)).stringValue();
			// write the contents to the file
			try {
				FileUtils.writeStringToFile(resultFile, resultContents);
			} catch (IOException e) {
				throw new CompilerException("Builder " + getName() + "failed to write result", e);
			}
		}
	}

	private boolean isWriteFile(final IStrategoTerm result) {
		if (result instanceof IStrategoAppl) {
			if (((IStrategoAppl) result).getName().equals("None")) {
				return false;
			} else {
				logger.fatal("Builder returned an unsupported result type {}", result);
				throw new CompilerException("Unsupported return value from builder: " + result);
			}
		} else {
			if (result == null || result.getSubtermCount() != 2
					|| !(result.getSubterm(0) instanceof IStrategoString)
					|| !(result.getSubterm(1) instanceof IStrategoString)) {
				logger.fatal("Builder returned an unsupported result type {}", result);
				throw new CompilerException("Unsupported return value from builder: " + result);
			} else {
				return true;
			}
		}
	}

	@Override
	public String toString() {
		return "Builder " + name + " invokes " + strategy + " on source " + onSource + " and meta "
				+ meta;
	}
}
