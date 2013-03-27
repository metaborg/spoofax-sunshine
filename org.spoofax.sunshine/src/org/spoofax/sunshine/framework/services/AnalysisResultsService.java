/**
 * 
 */
package org.spoofax.sunshine.framework.services;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.spoofax.sunshine.framework.messages.IAnalysisResult;
import org.spoofax.sunshine.framework.messages.IMessage;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class AnalysisResultsService {
	private static AnalysisResultsService INSTANCE;

	private AnalysisResultsService() {
	}

	public static AnalysisResultsService INSTANCE() {
		if (INSTANCE == null) {
			INSTANCE = new AnalysisResultsService();
		}
		return INSTANCE;
	}

	private final Map<File, IAnalysisResult> resultsMap = new HashMap<File, IAnalysisResult>();

	public void reset() {
		resultsMap.clear();
	}

	public IAnalysisResult getResult(File f) {
		assert f != null;
		return resultsMap.get(f);
	}

	public void addResult(IAnalysisResult res) {
		assert res != null;
		resultsMap.put(res.getFile(), res);
	}

	public Map<File, IAnalysisResult> getAllResultsMap() {
		return new HashMap<File, IAnalysisResult>(resultsMap);
	}

	public void commitMessages() {
		final Collection<IAnalysisResult> results = resultsMap.values();
		final Collection<IMessage> messages = new LinkedList<IMessage>();
		for (IAnalysisResult result : results) {
			messages.addAll(result.getMessages());
		}
		MessageService.INSTANCE().addMessage(messages);
	}
}
