/**
 * 
 */
package org.spoofax.sunshine;

import java.io.File;
import java.io.IOException;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.io.ParseTableManager;
import org.spoofax.sunshine.drivers.SunshineMainArguments;
import org.spoofax.terms.TermFactory;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class Environment {

	public final ITermFactory termFactory;
	public final ParseTableManager parseTableMgr;
	public File projectDir;
	private SunshineMainArguments mainArgs;

	// private LaunchConfiguration launchConfiguration;

	private static Environment INSTANCE;

	public static final Environment INSTANCE() {
		if (INSTANCE == null) {
			INSTANCE = new Environment();
		}
		return INSTANCE;
	}

	private Environment() {
		this.termFactory = new TermFactory().getFactoryWithStorageType(IStrategoTerm.MUTABLE);
		this.parseTableMgr = new ParseTableManager(termFactory);
	}

	public void setProjectDir(File pdir) {
		try {
			projectDir = pdir.getCanonicalFile();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public File getCacheDir() {
		assert projectDir != null;
		File cacheDir = new File(projectDir, ".cache");
		if (!cacheDir.exists()) {
			cacheDir.mkdir();
		}
		return cacheDir;
	}

	public boolean isStatEnabled() {
		return mainArgs.statstarget != null;
	}

	public void setMainArguments(SunshineMainArguments args) {
		this.mainArgs = args;
	}

	public SunshineMainArguments getMainArguments() {
		return this.mainArgs;
	}

}
