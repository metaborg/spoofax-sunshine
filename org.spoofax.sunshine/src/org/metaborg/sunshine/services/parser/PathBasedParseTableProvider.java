package org.metaborg.sunshine.services.parser;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.metaborg.sunshine.CompilerException;
import org.metaborg.sunshine.environment.LaunchConfiguration;
import org.metaborg.sunshine.environment.ServiceRegistry;
import org.metaborg.sunshine.parser.model.IParseTableProvider;
import org.spoofax.jsglr.client.ParseTable;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class PathBasedParseTableProvider implements IParseTableProvider {

	private final Path filePath;
	private final boolean caching;

	private ParseTable table;

	public PathBasedParseTableProvider(Path filePath) {
		this(filePath, true);
	}

	public PathBasedParseTableProvider(Path filePath, boolean caching) {
		assert filePath != null;
		this.filePath = filePath;
		this.caching = caching;
	}

	@Override
	public ParseTable getParseTable() {
		if (this.table != null)
			return this.table;

		InputStream stream;
		ParseTable table;
		try {
			stream = Files.newInputStream(filePath, StandardOpenOption.READ);
			table = ServiceRegistry.INSTANCE().getService(
					LaunchConfiguration.class).parseTableManager
					.loadFromStream(stream);
		} catch (Exception e) {
			throw new CompilerException("Could not load parse table", e);
		}

		if (caching) {
			this.table = table;
		}
		return table;
	}

	public String toString() {
		return filePath.toAbsolutePath().toString();
	}

	@Override
	public Path getPathToParseTable() {
		return filePath;
	}

}
