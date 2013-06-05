/**
 * 
 */
package org.spoofax.sunshine.services.messages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spoofax.sunshine.model.messages.IMessage;
import org.spoofax.sunshine.pipeline.connectors.ALinkManyToMany;
import org.spoofax.sunshine.pipeline.diff.Diff;
import org.spoofax.sunshine.pipeline.diff.DiffKind;
import org.spoofax.sunshine.pipeline.diff.MultiDiff;
import org.spoofax.sunshine.services.analyzer.AnalysisResult;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class MessageExtractorLink extends ALinkManyToMany<AnalysisResult, IMessage> {
	private static final Logger logger = LogManager.getLogger(MessageExtractorLink.class.getName());

	@Override
	public MultiDiff<IMessage> sinkWork(MultiDiff<AnalysisResult> input) {
		logger.trace("Selecting messages from {} inputs", input.size());
		final MultiDiff<IMessage> result = new MultiDiff<IMessage>();

		for (Diff<AnalysisResult> diff : input) {
			final DiffKind kind = diff.getDiffKind();
			for (IMessage msg : diff.getPayload().messages()) {
				result.add(new Diff<IMessage>(msg, kind));
			}
		}
		logger.trace("Messages selected");
		return result;
	}

}
