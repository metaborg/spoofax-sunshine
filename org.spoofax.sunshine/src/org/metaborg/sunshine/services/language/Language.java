/**
 * 
 */
package org.metaborg.sunshine.services.language;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metaborg.sunshine.parser.model.IParseTableProvider;
import org.metaborg.sunshine.services.parser.PathBasedParseTableProvider;
import org.metaborg.sunshine.services.pipelined.builders.Builder;
import org.metaborg.sunshine.services.pipelined.builders.IBuilder;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class Language extends ALanguage {

	private static final Logger logger = LogManager.getLogger(Language.class.getName());

	private final String[] extens;
	private final String startSymbol;
	private final PathBasedParseTableProvider parseTableProvider;
	private final String analysisFunction;
	private final Path[] compilerFiles;
	private final Map<String, IBuilder> builders = new HashMap<>();
	private Path definitionDir;

	public Language(String name, String[] extens, String startSymbol, Path parseTable,
			String analysisFunction, Path[] compilerFiles, Path definitionDir) {
		super(name);

		assert name != null && name.length() > 0;
		assert extens != null && extens.length > 0;
		assert startSymbol != null && startSymbol.length() > 0;
		assert parseTable != null;
		assert analysisFunction != null && analysisFunction.length() > 0;
		assert compilerFiles != null && compilerFiles.length > 0;
		assert builders != null;
		assert definitionDir != null && definitionDir.toFile().exists();

		this.extens = extens;
		this.startSymbol = startSymbol;
		this.parseTableProvider = new PathBasedParseTableProvider(parseTable);
		this.analysisFunction = analysisFunction;
		this.compilerFiles = compilerFiles;
		this.definitionDir = definitionDir;
	}

	@Override
	public Collection<String> getFileExtensions() {
		return Arrays.asList(extens);
	}

	@Override
	public String getStartSymbol() {
		return this.startSymbol;
	}

	@Override
	public IParseTableProvider getParseTableProvider() {
		return this.parseTableProvider;
	}

	@Override
	public String getAnalysisFunction() {
		return analysisFunction;
	}

	@Override
	public Path[] getCompilerFiles() {
		return this.compilerFiles;
	}

	@Override
	public void registerBuilder(String name, String strategyName, boolean onSource,
			boolean meta) {
		logger.trace("Registering builder {} to strategy {}", name, strategyName);
		if (builders.containsKey(name)) {
			logger.warn("Overriding previous registration of builder {}", name);
		}
		builders.put(name, new Builder(name, strategyName, this, onSource, meta));
	}

	@Override
	public IBuilder getBuilder(String name) {
		return builders.get(name);
	}

	@Override
	public Path getDefinitionPath() {
		return definitionDir;
	}

	@Override
	public String toString() {
		String s = super.toString();
		s += "Builders: \n";
		for (IBuilder builder : builders.values()) {
			s += "\t" + builder.toString() + "\n";
		}
		return s;
	}


}
