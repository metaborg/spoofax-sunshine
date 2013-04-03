/**
 * 
 */
package org.spoofax.sunshine.statistics;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public interface IValidatable {

	public final IValidatable NEVER_VALIDATABLE = new IValidatable() {

		@Override
		public boolean validate() {
			return false;
		}
	};
	
	public final IValidatable ALWAYS_VALIDATABLE = new IValidatable() {

		@Override
		public boolean validate() {
			return true;
		}
	};

	public boolean validate();
}
