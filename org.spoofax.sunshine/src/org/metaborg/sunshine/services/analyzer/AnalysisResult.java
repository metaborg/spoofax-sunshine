package org.metaborg.sunshine.services.analyzer;

import java.util.Collection;

import org.metaborg.sunshine.services.language.ALanguage;

public class AnalysisResult {
	public final ALanguage language;
	public final Collection<AnalysisFileResult> fileResults;
	public final Collection<String> affectedPartitions;
	public final AnalysisDebugResult debugResult;
	public final AnalysisTimeResult timeResult;

	public AnalysisResult(ALanguage language,
			Collection<AnalysisFileResult> fileResults,
			Collection<String> affectedPartitions,
			AnalysisDebugResult debugResult, AnalysisTimeResult timeResult) {
		this.language = language;
		this.fileResults = fileResults;
		this.affectedPartitions = affectedPartitions;
		this.debugResult = debugResult;
		this.timeResult = timeResult;
	}
}
