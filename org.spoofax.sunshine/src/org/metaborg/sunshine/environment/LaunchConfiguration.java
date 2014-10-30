/**
 * 
 */
package org.metaborg.sunshine.environment;

import java.io.IOException;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.resource.IResourceService;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.io.ParseTableManager;
import org.spoofax.terms.TermFactory;

import com.google.inject.Inject;

/**
 * @author vladvergu
 * 
 */
public class LaunchConfiguration {
	public final ITermFactory termFactory;
	public final ParseTableManager parseTableManager;
	public final SunshineMainArguments mainArguments;
	public final FileObject projectDir;
	public final FileObject cacheDir;

	@Inject
	public LaunchConfiguration(SunshineMainArguments mainArguments,
			IResourceService resourceService) {
		this.termFactory = new TermFactory()
				.getFactoryWithStorageType(IStrategoTerm.MUTABLE);
		this.parseTableManager = new ParseTableManager(termFactory);
		this.mainArguments = mainArguments;
		this.projectDir = resourceService.resolve(mainArguments.project);
		try {
			this.cacheDir = this.projectDir.resolveFile(".cache");
			if (!cacheDir.exists()) {
				cacheDir.createFolder();
			}
		} catch (IOException e) {
			throw new RuntimeException(
					"Can't resolve or create cache directory", e);
		}
	}
}
