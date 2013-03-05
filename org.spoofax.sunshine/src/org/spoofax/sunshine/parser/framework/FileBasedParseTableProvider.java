package org.spoofax.sunshine.parser.framework;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.spoofax.jsglr.client.ParseTable;
import org.spoofax.sunshine.Environment;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 *
 */
public class FileBasedParseTableProvider implements IParseTableProvider {

	private final File file;
	private final boolean caching;
	
	private ParseTable table;
	
	public FileBasedParseTableProvider(File f){
		this.file = f;
		this.caching = true;
	}
	
	public FileBasedParseTableProvider(File f, boolean caching) {
		this.file = f;
		this.caching = caching;
	}
	
	@Override
	public ParseTable getParseTable() throws ParserException {
		if(this.table != null)
			return this.table;
		
		InputStream stream;
		ParseTable table;
		try {
			stream = new FileInputStream(file);
			table = Environment.INSTANCE().parseTableMgr.loadFromStream(stream);
		} catch (Exception e) {
			throw new ParserException(e);
		}
	
		if(caching){
			this.table = table;  
		}
		return table;
	}

}
