/**
 * 
 */
package org.metaborg.sunshine.services.builders;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.interpreter.terms.ITermFactory;

/**
 * A Java representation of the input term to a builder. It is in fact a wrapper over the following:
 * 
 * <code>
 * (node, position, ast, path, project-path)
 * </code>
 * 
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class BuilderInputTerm {
	private static final Logger logger = LogManager.getLogger(BuilderInputTerm.class.getName());

	private final ITermFactory termFactory;
	private final IStrategoTerm node;
	private final IStrategoList position;
	private final IStrategoTerm ast;
	private final File file;
	private final File project;

	public BuilderInputTerm(ITermFactory factory, IStrategoTerm ast, File file, File project) {
		this(factory, ast, factory.makeList(), ast, file, project);
	}

	public BuilderInputTerm(ITermFactory factory, IStrategoTerm node, IStrategoList position,
			IStrategoTerm ast, File file, File project) {
		this.termFactory = factory;
		this.node = node;
		this.position = position;
		this.ast = ast;
		this.file = file;
		this.project = project;
		logger.trace("New instance created for position {} of file {} in project {}", position,
				file, project);
	}

	public IStrategoTuple toStratego() {
		logger.trace("Converting input for position {} of file {} in project {} to Stratego tuple",
				position, file, project);
		return termFactory.makeTuple(node, position, ast,
				termFactory.makeString(project.toURI().relativize(file.toURI()).toString()),
				termFactory.makeString(project.getAbsolutePath()));
	}

	public IStrategoTerm getNode() {
		return node;
	}

	public IStrategoList getPosition() {
		return position;
	}

	public IStrategoTerm getAst() {
		return ast;
	}

	public File getFile() {
		return file;
	}

	public File getProject() {
		return project;
	};
}
