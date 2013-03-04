package org.strategoxt.imp.runtime.parser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.spoofax.jsglr.client.InvalidParseTableException;
import org.spoofax.jsglr.client.ParseTable;
import org.spoofax.sunshine.parser.framework.IParseTableProvider;
import org.strategoxt.imp.runtime.Debug;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.OLDSGLRParseController;

/**
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 */
public class ParseTableProvider implements IParseTableProvider {
	
	private final IFile file;
	
	private final Descriptor descriptor;
	
	private ParseTable table;
	
	public ParseTableProvider(IFile file) {
		this.file = file;
		this.descriptor = null;
	}
	
	public ParseTableProvider(Descriptor descriptor) {
		this.descriptor = descriptor;
		this.file = null;
	}
	
	public ParseTableProvider(ParseTable table) {
		this.table = table;
		this.descriptor = null;
		this.file = null;
	}
	
	public ParseTable getParseTable() throws BadDescriptorException, InvalidParseTableException, IOException, CoreException {
		if (table != null) return table;
		Debug.startTimer();
		InputStream stream = file == null ? descriptor.openParseTableStream() : file.getContents(true);
		ParseTable table = Environment.loadParseTable(stream);
		Debug.stopTimer("Loaded parse table for " + (file == null ? descriptor.getLanguage().getName() : file.getName()));
		return this.table = table;
	}
	
}
