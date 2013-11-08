/**
 * 
 */
package org.metaborg.sunshine.statistics;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public interface IValidatable<T> {

	public final IValidatable<Boolean> NEVER_VALIDATABLE = new IValidatable<Boolean>() {

		@Override
		public boolean validate() {
			return false;
		}

		public Boolean getValue() {
			return false;
		};
	};

	public final IValidatable<Boolean> ALWAYS_VALIDATABLE = new IValidatable<Boolean>() {

		@Override
		public boolean validate() {
			return true;
		}

		public Boolean getValue() {
			return true;
		};

	};

	public abstract boolean validate();

	public T getValue();

}