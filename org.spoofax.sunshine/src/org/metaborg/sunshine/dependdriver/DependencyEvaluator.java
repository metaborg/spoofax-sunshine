/**
 * 
 */
package org.metaborg.sunshine.dependdriver;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metaborg.sunshine.CompilerException;

/**
 * @author vladvergu
 * 
 */
public class DependencyEvaluator {

	private static final Logger logger = LogManager
			.getLogger(DependencyEvaluator.class.getName());

	private boolean evaluationStarted;

	private final List<IActionableDependency> dependencies = new ArrayList<>();

	public DependencyEvaluator() {

	}

	public void addDependency(IActionableDependency dependency) {
		Objects.requireNonNull(dependency, "Invalid null dependency");
		if (evaluationStarted) {
			logger.fatal("Cannot add dependency after evaluation has been started");
			throw new CompilerException(
					"Cannot add dependency after evaluation has been started");
		}
		dependencies.add(dependency);
	}

	public boolean evaluateDependencies() {
		evaluationStarted = true;
		// int currentExecutionIndex = dependencies.size() > 0 ? 0 : -1;
		int currentExecutionIndex = getNextExecutionIndex(-1);
		while (currentExecutionIndex >= 0) {
			logger.debug("Evaluating dependency {} of {}",
					currentExecutionIndex + 1, dependencies.size());
			boolean evalResult = dependencies.get(currentExecutionIndex)
					.execute();
			if (!evalResult)
				return false;
			currentExecutionIndex = getNextExecutionIndex(currentExecutionIndex);
		}

		return true;
	}

	private int getNextExecutionIndex(int currentIndex) {
		boolean updateRequired = false;
		for (IActionableDependency dependency : dependencies) {
			if (dependency.isUpdateRequired()) {
				updateRequired = true;
				break;
			}
		}
		if (!updateRequired) {
			return -1;
		}
		boolean nextFound = false;
		int nextIndex = currentIndex + 1;
		for (; nextIndex < dependencies.size(); nextIndex++) {
			IActionableDependency dependency = dependencies.get(nextIndex);
			if (dependency.isUpdateRequired() && dependency.isReadyToRun()) {
				nextFound = true;
				break;
			}
		}
		if (nextFound)
			return nextIndex;
		nextIndex = 0;
		for (; nextIndex < currentIndex; nextIndex++) {
			IActionableDependency dependency = dependencies.get(nextIndex);
			if (dependency.isUpdateRequired() && dependency.isReadyToRun()) {
				nextFound = true;
				break;
			}
		}
		if (nextFound)
			return nextIndex;

		logger.fatal(
				"Loop detected! Stuck evaluating dependency with index {}",
				currentIndex);
		throw new CompilerException(
				"Loop detected! Stuck evaluating dependency with index "
						+ currentIndex);
	}

}
