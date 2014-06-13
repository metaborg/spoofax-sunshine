package org.metaborg.sunshine.services.analyzer;

import org.spoofax.interpreter.terms.IStrategoList;

public class AnalysisDebugResult {
	public final int indexEntriesRemoved;
	public final int indexEntriesAdded;
	public final int tasksRemoved;
	public final int tasksAdded;
	public final int tasksInvalidated;

	public final IStrategoList evaluatedTasks;
	public final IStrategoList skippedTasks;
	public final IStrategoList unevaluatedTasks;

	public AnalysisDebugResult(int indexEntriesRemoved, int indexEntriesAdded,
			int tasksRemoved, int tasksAdded, int tasksInvalidated,
			IStrategoList evaluatedTasks, IStrategoList skippedTasks,
			IStrategoList unevaluatedTasks) {
		this.indexEntriesRemoved = indexEntriesRemoved;
		this.indexEntriesAdded = indexEntriesAdded;
		this.tasksRemoved = tasksRemoved;
		this.tasksAdded = tasksAdded;
		this.tasksInvalidated = tasksInvalidated;
		this.evaluatedTasks = evaluatedTasks;
		this.skippedTasks = skippedTasks;
		this.unevaluatedTasks = unevaluatedTasks;
	}
}
