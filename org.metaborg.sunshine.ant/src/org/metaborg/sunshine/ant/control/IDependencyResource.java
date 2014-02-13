/**
 * 
 */
package org.metaborg.sunshine.ant.control;

import java.io.File;

/**
 * @author vladvergu
 * 
 */
public interface IDependencyResource {

	public File[] getFileset();

	public long getLastModification();
}
