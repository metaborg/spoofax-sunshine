/**
 * 
 */
package org.spoofax.sunshine.util;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.spoofax.sunshine.pipeline.diff.DiffKind;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class DiffingHashMap<K, V> extends AbstractMap<K, V> {

	private final IMerger<V> merger;
	private BubblingMap<K, V> map;
	private volatile boolean nowDiffing;
	private Map<K, DiffKind> changes;

	public DiffingHashMap(IMerger<V> merger) {
		this.merger = merger;
		this.map = new BubblingMap<K, V>(null, new HashMap<K, V>());
	}

	public void beginDiff() {
		if (nowDiffing)
			endDiff();
		nowDiffing = true;
		changes = new HashMap<K, DiffKind>();
		map = new BubblingMap<K, V>(map.top(), new HashMap<K, V>());
	}

	public Map<K, DiffKind> endDiff() {
		if (!nowDiffing) {
			return new HashMap<K, DiffKind>();
		}
		Map<K, V> dropped = map.dropBottom();
		for (Entry<K, V> drop : dropped.entrySet()) {
			changes.put(drop.getKey(), DiffKind.DELETION);
		}
		Map<K, DiffKind> savedChanges = changes;
		changes = null;
		nowDiffing = false;
		return savedChanges;
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		return map.entrySet();
	}

	@Override
	public V put(K key, V newValue) {
		V oldValue = map.get(key);
		if (nowDiffing) {
			if (oldValue == null) {
				if (changes.containsKey(key)) {
					changes.put(key, DiffKind.MODIFICATION);
				} else {
					changes.put(key, DiffKind.ADDITION);
				}
			} else {
				if (merger.areDifferent(oldValue, newValue)) {
					if (changes.get(key) != DiffKind.ADDITION) {
						changes.put(key, DiffKind.MODIFICATION);
					}
				}
			}
		}
		return map.put(key, oldValue != null ? merger.merge(oldValue, newValue) : newValue);
	}

	@SuppressWarnings("unchecked")
	@Override
	public V remove(Object key) {
		try {
			if (nowDiffing) {
				if (map.containsKey(key)) {
					changes.put((K) key, DiffKind.DELETION);
				}
			}
		} catch (ClassCastException castex) {
			// nothing to do here. it is normal if the key is not a K
		}
		return map.remove(key);
	}

	@Override
	public V get(Object key) {
		return map.get(key);
	}

	@Override
	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}
}
