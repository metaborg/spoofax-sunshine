package org.metaborg.sunshine.services.filesource;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.metaborg.spoofax.core.resource.DefaultFileSystemManagerProvider;
import org.metaborg.sunshine.environment.SunshineMainArguments;

import com.google.inject.Inject;

public class SunshineFileSystemManagerProvider extends
		DefaultFileSystemManagerProvider {
	private final SunshineMainArguments mainArguments;

	@Inject
	public SunshineFileSystemManagerProvider(SunshineMainArguments mainArguments) {
		super();

		this.mainArguments = mainArguments;
	}

	@Override
	protected void setBaseFile(DefaultFileSystemManager manager)
			throws FileSystemException {
		try {
			// Absolute path
			final FileObject direct = manager
					.resolveFile(mainArguments.project);
			manager.setBaseFile(direct);
		} catch (FileSystemException e) {
			// Relative path
			final FileObject current = manager.resolveFile(System
					.getProperty("user.dir"));
			manager.setBaseFile(current.resolveFile(mainArguments.project));
		}
	}
}
