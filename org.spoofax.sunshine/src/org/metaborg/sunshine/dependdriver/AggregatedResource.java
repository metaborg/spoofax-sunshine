/**
 * 
 */
package org.metaborg.sunshine.dependdriver;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author vladvergu
 * 
 */
public class AggregatedResource implements IResource {

	private static final Logger logger = LogManager
			.getLogger(AggregatedResource.class.getName());

	public final List<IResource> aggregatedResources;

	public AggregatedResource(Collection<IResource> resources) {
		aggregatedResources = new ArrayList<>(resources);
		logger.trace("Aggregating {} resources", resources.size());
	}

	@Override
	public boolean isEmpty() throws IOException {
		for (IResource resource : aggregatedResources) {
			if (!resource.isEmpty()) {
				logger.trace("Resource is not empty");
				return false;
			}
		}
		logger.trace("Resource is empty");
		return true;
	}

	@Override
	public Set<Path> getFileset() throws IOException {
		Set<Path> paths = new HashSet<>();
		for (IResource resource : aggregatedResources) {
			paths.addAll(resource.getFileset());
		}
		logger.debug("{} files matched in this resource", paths.size());
		return paths;
	}

	@Override
	public long getLastModification() throws IOException {
		long result = -1;
		for (IResource resource : aggregatedResources) {
			long lastModified = resource.getLastModification();
			result = lastModified > result ? lastModified : result;
		}
		logger.debug("Determined {} as last modification time", result);
		return result;
	}

}
