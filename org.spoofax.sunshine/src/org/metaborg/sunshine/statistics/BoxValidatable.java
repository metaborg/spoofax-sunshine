/**
 * 
 */
package org.metaborg.sunshine.statistics;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class BoxValidatable<T> implements IValidatable<T> {

	public T value;

	public BoxValidatable(T value) {
		this.value = value;
	}

	@Override
	public boolean validate() {
		return true;
	}

	@Override
	public T getValue() {
		return value;
	}

}
