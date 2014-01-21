/**
 * 
 */
package org.metaborg.sunshine.environment;

import java.io.File;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.io.ParseTableManager;
import org.spoofax.terms.TermFactory;

/**
 * @author vladvergu
 * 
 */
public class LaunchConfiguration {

	public final ITermFactory termFactory;
	public final ParseTableManager parseTableManager;
	public final SunshineMainArguments mainArguments;
	public final File projectDir;

	public LaunchConfiguration(SunshineMainArguments mainArguments,
			File projectDir) {
		this.termFactory = new TermFactory()
				.getFactoryWithStorageType(IStrategoTerm.MUTABLE);
		this.parseTableManager = new ParseTableManager(termFactory);
		this.mainArguments = mainArguments;
		this.projectDir = projectDir;
	}

	public File getCacheDir() {
		assert projectDir != null;
		File cacheDir = new File(projectDir, ".cache");
		if (!cacheDir.exists()) {
			cacheDir.mkdir();
		}
		return cacheDir;
	}
}
