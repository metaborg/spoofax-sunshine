/**
 * 
 */
package org.spoofax.sunshine.pipeline.services;

import org.spoofax.sunshine.model.messages.IMessage;
import org.spoofax.sunshine.parser.model.IStrategoParseOrAnalyzeResult;
import org.spoofax.sunshine.pipeline.connectors.ALinkManyToMany;
import org.spoofax.sunshine.pipeline.diff.Diff;
import org.spoofax.sunshine.pipeline.diff.MultiDiff;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 *
 */
public class MessageExtractorLink extends
	ALinkManyToMany<IStrategoParseOrAnalyzeResult, IMessage> {

    @Override
    public MultiDiff<IMessage> sinkWork(
	    MultiDiff<IStrategoParseOrAnalyzeResult> input) {
	final MultiDiff<IMessage> result = new MultiDiff<IMessage>();

	for (Diff<IStrategoParseOrAnalyzeResult> diff : input) {
	    // TODO implement this
	    // result.add(new Diff<IMessage>(diff.getPayload().me))
	}

	return result;
    }

}
