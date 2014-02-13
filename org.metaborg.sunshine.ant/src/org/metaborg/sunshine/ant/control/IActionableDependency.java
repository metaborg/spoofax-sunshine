/**
 * 
 */
package org.metaborg.sunshine.ant.control;

/**
 * @author vladvergu
 * 
 */
public interface IActionableDependency {

	/**
	 * Executes this dependency. This typically would mean applying some
	 * transformations on the input {@link IDependencyResource} in order to
	 * obtain updated output {@link IDependencyResource}.
	 * 
	 * @return <code>true</code> if the execution of the required task was
	 *         successful, or <code>false</code> if errors where encountered
	 * 
	 */
	public boolean execute();

	/**
	 * Indicates whether this {@link IActionableDependency} needs to be
	 * evaluated using the {@link #execute()} method. The need to evaluate this
	 * task is typically indicated by the output {@link IDependencyResource}
	 * being out of date with respect to the input {@link IDependencyResource}.
	 * 
	 * @return <code>true</code> if this dependency needs to be evaluated,
	 *         <code>false</code> otherwise
	 */
	public boolean isUpdateRequired();

	/**
	 * Indicates whether this {@link IActionableDependency} is ready to
	 * evaluate. This does not depend on the value returned by
	 * {@link #isUpdateRequired()}. This typically is influenced by whether the
	 * input {@link IDependencyResource} has become available. If we are talking
	 * about files then the {@link IDependencyResource} needs to be non-empty.
	 * 
	 * @return <code>true</code> if this dependency can be evaluated,
	 *         <code>false</code> otherwise
	 */
	public boolean isReadyToRun();
}
