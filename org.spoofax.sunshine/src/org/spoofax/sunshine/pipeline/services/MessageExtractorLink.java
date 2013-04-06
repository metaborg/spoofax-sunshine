/**
 * 
 */
package org.spoofax.sunshine.pipeline.services;

import org.spoofax.sunshine.model.messages.IMessage;
import org.spoofax.sunshine.parser.model.IStrategoParseOrAnalyzeResult;
import org.spoofax.sunshine.pipeline.connectors.ALinkManyToMany;
import org.spoofax.sunshine.pipeline.diff.Diff;
import org.spoofax.sunshine.pipeline.diff.DiffKind;
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
	    final DiffKind kind = diff.getDiffKind();
	    for(IMessage msg : diff.getPayload().messages()){
		result.add(new Diff<IMessage>(msg, kind));
	    }
	}

	return result;
    }

}
