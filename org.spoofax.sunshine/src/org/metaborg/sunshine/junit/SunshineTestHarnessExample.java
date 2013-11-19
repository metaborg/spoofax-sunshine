package org.metaborg.sunshine.junit;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import org.junit.Test;

/**
 * Just an example of how to use the {@link LanguageTestHarness}
 * 
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class SunshineTestHarnessExample extends LanguageTestHarness {

	@Override
	public Path getPathToLanguageRepository() {
		return FileSystems.getDefault().getPath(
				"/Users/vladvergu/Documents/workspaces/strategoxt-dev/entity/include");
	}

	@Override
	public Path getPathToInputFile() {
		return FileSystems.getDefault().getPath(
				"/Users/vladvergu/Documents/workspaces/strategoxt-dev/entity/test/example.ent");
	}

	@Test
	public void testParseFileSucceeds() throws IOException {
		assertParseSucceeds(getPathToInputFile().toFile());
	}

	@Test
	public void testParseFileFails() throws IOException {
		assertParseFails(getPathToInputFile().toFile());
	}

	@Test
	public void testAnalyzeFileSucceeds() throws IOException {
		assertAnalysisSucceeds(getPathToInputFile().toFile());
	}

	@Test
	public void testAnalyzeFileFails() throws IOException {
		assertAnalysisFails(getPathToInputFile().toFile());
	}


}
