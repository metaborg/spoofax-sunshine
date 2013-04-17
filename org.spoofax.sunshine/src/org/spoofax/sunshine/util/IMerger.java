/**
 * 
 */
package org.spoofax.sunshine.util;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public interface IMerger<T> {

    public boolean areDifferent(T older, T newer);

    public T merge(T older, T newer);
}
