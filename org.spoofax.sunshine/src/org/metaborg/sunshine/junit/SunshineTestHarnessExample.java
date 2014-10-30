package org.metaborg.sunshine.junit;

import java.io.IOException;

import org.apache.commons.vfs2.FileObject;
import org.junit.Test;

/**
 * Just an example of how to use the {@link LanguageTestHarness}
 * 
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class SunshineTestHarnessExample extends LanguageTestHarness {

	@Override
	public FileObject getPathToLanguageRepository() {
		return resourceService
				.resolve("/Users/vladvergu/Documents/workspaces/strategoxt-dev/entity/include");
	}

	@Override
	public FileObject getPathToInputFile() {
		return resourceService
				.resolve("/Users/vladvergu/Documents/workspaces/strategoxt-dev/entity/test/example.ent");
	}

	@Test
	public void testParseFileSucceeds() throws IOException {
		assertParseSucceeds(getPathToInputFile());
	}

	@Test
	public void testParseFileFails() throws IOException {
		assertParseFails(getPathToInputFile());
	}

	@Test
	public void testAnalyzeFileSucceeds() throws IOException {
		assertAnalysisSucceeds(getPathToInputFile());
	}

	@Test
	public void testAnalyzeFileFails() throws IOException {
		assertAnalysisFails(getPathToInputFile());
	}

}
