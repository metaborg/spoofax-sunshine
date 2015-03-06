package org.metaborg.sunshine.services.pipelined.builders;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 */
public class BuilderInputTerm {
    private static final Logger logger = LoggerFactory.getLogger(BuilderInputTerm.class.getName());

    private final ITermFactory termFactory;
    private final IStrategoTerm node;
    private final IStrategoList position;
    private final IStrategoTerm ast;
    private final FileObject file;
    private final FileObject project;

    public BuilderInputTerm(ITermFactory factory, IStrategoTerm ast, FileObject file, FileObject project) {
        this(factory, ast, factory.makeList(), ast, file, project);
    }

    public BuilderInputTerm(ITermFactory factory, IStrategoTerm node, IStrategoList position, IStrategoTerm ast,
        FileObject file, FileObject project) {
        this.termFactory = factory;
        this.node = node;
        this.position = position;
        this.ast = ast;
        this.file = file;
        this.project = project;
        logger.trace("New instance created for position {} of file {} in project {}", position, file, project);
    }

    public IStrategoTuple toStratego() throws FileSystemException {
        logger.trace("Converting input for position {} of file {} in project {} to Stratego tuple", position, file,
            project);
        return termFactory.makeTuple(node, position, ast,
            termFactory.makeString(project.getName().getRelativeName(file.getName())),
            termFactory.makeString(project.getName().getPath()));
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

    public FileObject getFile() {
        return file;
    }

    public FileObject getProject() {
        return project;
    };
}
