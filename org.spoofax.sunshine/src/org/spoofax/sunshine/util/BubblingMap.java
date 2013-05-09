/**
 * 
 */
package org.spoofax.sunshine.util;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class BubblingMap<K, V> extends AbstractMap<K, V> {

    private final Map<K, V> top;
    private Map<K, V> bottom;

    public BubblingMap(Map<K, V> bottom, Map<K, V> top) {
	this.bottom = bottom;
	this.top = top;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
	return new AbstractSet<Entry<K, V>>() {

	    @Override
	    public Iterator<Entry<K, V>> iterator() {
		return new Iterator<Entry<K, V>>() {
		    private Iterator<Entry<K, V>> topIter = top.entrySet()
			    .iterator();
		    private Iterator<Entry<K, V>> bottomIter = bottom != null ? bottom
			    .entrySet().iterator() : null;
		    private boolean inTop = true;

		    @Override
		    public boolean hasNext() {
			if (bottomIter != null && bottom == null) {
			    throw new ConcurrentModificationException();
			}
			return topIter.hasNext()
				|| (bottomIter != null && bottomIter.hasNext());
		    }

		    @Override
		    public Entry<K, V> next() {
			if (bottomIter != null && bottom == null) {
			    throw new ConcurrentModificationException();
			}
			if (inTop) {
			    if (topIter.hasNext()) {
				return topIter.next();
			    } else {
				inTop = false;
			    }
			}
			if (bottomIter != null) {
			    return bottomIter.next();
			} else {
			    throw new NoSuchElementException();
			}
		    }

		    @Override
		    public void remove() {
			if (bottomIter != null && bottom == null) {
			    throw new ConcurrentModificationException();
			}
			if (inTop) {
			    topIter.remove();
			} else if (bottomIter != null) {
			    bottomIter.remove();
			} else {
			    throw new NoSuchElementException();
			}
		    }
		};
	    }

	    @Override
	    public int size() {
		return (bottom != null ? bottom.size() : 0) + top.size();
	    }

	};
    }

    @Override
    public V put(K key, V newValue) {
	V oldValue = top.get(key);
	if (oldValue == null && bottom != null) {
	    oldValue = bottom.remove(key);
	}
	top.put(key, newValue);
	return oldValue;
    }

    @Override
    public V get(Object key) {
	V value = top.get(key);
	if (value == null && bottom != null) {
	    value = bottom.get(key);
	}
	return value;
    }

    @Override
    public V remove(Object key) {
	V removed = top.remove(key);
	if (removed == null && bottom != null)
	    removed = bottom.remove(key);
	return removed;
    }

    @Override
    public boolean containsKey(Object key) {
		return top.containsKey(key)
				|| (bottom != null && bottom.containsKey(key));
    }

    public Map<K, V> top() {
	return top;
    }

    public Map<K, V> bottom() {
	return bottom;
    }

    public Map<K, V> dropBottom() {
	Map<K, V> oldBottom = bottom;
	bottom = null;
	return oldBottom;
    }

}
