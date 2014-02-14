/**
 * 
 */
package org.metaborg.sunshine.dependdriver;

/**
 * @author vladvergu
 * 
 */
public interface IActionableDependency {

	/**
	 * Executes this dependency. This typically would mean applying some
	 * transformations on the input {@link IResource} in order to
	 * obtain updated output {@link IResource}.
	 * 
	 * @return <code>true</code> if the execution of the required task was
	 *         successful, or <code>false</code> if errors where encountered
	 * 
	 */
	public boolean execute();

	/**
	 * Indicates whether this {@link IActionableDependency} needs to be
	 * evaluated using the {@link #execute()} method. The need to evaluate this
	 * task is typically indicated by the output {@link IResource}
	 * being out of date with respect to the input {@link IResource}.
	 * 
	 * @return <code>true</code> if this dependency needs to be evaluated,
	 *         <code>false</code> otherwise
	 */
	public boolean isUpdateRequired();

	/**
	 * Indicates whether this {@link IActionableDependency} is ready to
	 * evaluate. This does not depend on the value returned by
	 * {@link #isUpdateRequired()}. This typically is influenced by whether the
	 * input {@link IResource} has become available. If we are talking
	 * about files then the {@link IResource} needs to be non-empty.
	 * 
	 * @return <code>true</code> if this dependency can be evaluated,
	 *         <code>false</code> otherwise
	 */
	public boolean isReadyToRun();
}
