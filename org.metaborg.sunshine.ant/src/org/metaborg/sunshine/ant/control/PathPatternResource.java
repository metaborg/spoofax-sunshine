/**
 * 
 */
package org.metaborg.sunshine.ant.control;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author vladvergu
 * 
 */
public class PathPatternResource implements IResource {

	private static final Logger logger = LogManager
			.getLogger(PathPatternResource.class.getName());

	private final Path parent;
	private final PathMatcher matcher;

	public PathPatternResource(Path parent, String pattern) {
		this.parent = parent;
		this.matcher = FileSystems.getDefault().getPathMatcher(
				"glob:" + pattern);
		logger.trace("Created for parent {}", parent);
	}

	@Override
	public Set<Path> getFileset() throws IOException {
		final Set<Path> matches = new HashSet<>();
		Files.walkFileTree(parent, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file,
					BasicFileAttributes attrs) throws IOException {
				if (matcher.matches(file)) {
					matches.add(file);
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc)
					throws IOException {
				return FileVisitResult.CONTINUE;
			}
		});
		return matches;
	}

	public boolean isEmpty() throws IOException {
		return getFileset().isEmpty();
	}

	@Override
	public long getLastModification() throws IOException {
		Set<Path> fileSet = getFileset();
		long result = -1;
		for (Path path : fileSet) {
			long lastModified = path.toFile().lastModified();
			result = lastModified > result ? lastModified : result;
		}
		logger.debug("Determined {} as last modification time", result);
		return result;
	}

}
