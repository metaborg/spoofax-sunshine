package org.spoofax.sunshine.statistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * 
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class DataRecording {

    private volatile boolean open = true;

    private final Map<String, IValidatable<?>> validatables = new HashMap<String, IValidatable<?>>();
    private final Map<String, Long> points = new HashMap<String, Long>();
    private final Stack<RunningTimer> timers = new Stack<RunningTimer>();

    public void close() {
	open = false;
    }

    protected void addDataPoint(String name, long value) {
	assert open;
	assert validatables.get(name) == null;
	Long oldValue = points.get(name);
	if (oldValue == null)
	    oldValue = 0L;
	points.put(name, value + oldValue);
    }

    protected void startTimer(String name) {
	timers.push(new RunningTimer(name, System.currentTimeMillis()));
    }

    protected void stopTimer() {
	RunningTimer timer = timers.pop();
	points.put(timer.name, System.currentTimeMillis() - timer.time);
    }

    protected void addDataPoint(String name, IValidatable<?> value) {
	assert open;
	assert points.get(name) == null;
	if (!open) {
	    throw new RuntimeException(
		    "Attempting to add data point to closed recording");
	}
	validatables.put(name, value);
    }

    public Set<String> getKeys() {
	final Set<String> res = new HashSet<String>();
	res.addAll(validatables.keySet());
	res.addAll(points.keySet());
	return res;
    }

    public List<String> toOrderedStrings(List<String> keys, String defaultValue) {
	final List<String> result = new ArrayList<String>(keys.size());

	for (String key : keys) {
	    final Long point = points.get(key);
	    final IValidatable<?> validatable = validatables.get(key);
	    assert !(point != null && validatable != null);
	    if (point != null) {
		result.add(point.toString());
	    } else if (validatable != null) {
		result.add("" + validatable.getValue());
	    } else {
		result.add(defaultValue);
	    }
	}
	return result;
    }

    private class RunningTimer {
	public String name;
	public long time;

	public RunningTimer(String name, long time) {
	    this.name = name;
	    this.time = time;
	}
    }

}
