/**
 * 
 */
package org.spoofax.sunshine.util;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public final class NoMerger<T> implements IMerger<T> {

	@Override
	public boolean areDifferent(T older, T newer) {
		return true;
	}

	@Override
	public T merge(T older, T newer) {
		return newer;
	}

}
