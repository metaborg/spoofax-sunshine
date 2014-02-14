/**
 * 
 */
package org.metaborg.sunshine.ant.control;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

/**
 * @author vladvergu
 * 
 */
public interface IResource {

	public boolean isEmpty() throws IOException;

	public Set<Path> getFileset() throws IOException;

	public long getLastModification() throws IOException;
}
