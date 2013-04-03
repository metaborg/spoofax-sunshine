package org.spoofax.sunshine.statistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class DataRecording {

	private volatile boolean open = true;

	private final Map<String, IValidatable> validatables = new HashMap<String, IValidatable>();
	private final Map<String, Long> points = new HashMap<String, Long>();

	public void close() {
		open = false;
	}

	public void addDataPoint(String name, long value) {
		assert open;
		assert validatables.get(name) == null;
		points.put(name, value);
	}

	public void addDataPoint(String name, IValidatable value) {
		assert open;
		assert points.get(name) == null;
		validatables.put(name, value);
	}

	public Set<String> getKeys() {
		final Set<String> res = new HashSet<String>(validatables.keySet());
		res.addAll(points.keySet());
		return res;
	}

	public List<String> toOrderedStrings(List<String> keys, String defaultValue) {
		final List<String> result = new ArrayList<String>(keys.size());
		
		for (String key : keys) {
			final Long point = points.get(key);
			final IValidatable validatable = validatables.get(key);
			assert !(point != null && validatable != null);
			if (point != null) {
				result.add(point.toString());
			} if (validatable != null) {
				result.add("" + (validatable.validate() ? 1 : 0));
			} else {
				result.add(defaultValue);
			}
		}
		return result;
	}

}
