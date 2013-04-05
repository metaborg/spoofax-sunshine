/**
 * 
 */
package org.spoofax.sunshine.framework.services.pipeline.servicewrappers;

import java.io.File;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.sunshine.framework.services.ParseService;
import org.spoofax.sunshine.framework.services.pipeline.ALinkOneToOneSequential;
import org.spoofax.sunshine.framework.services.pipeline.diff.Diff;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class ParserLink extends ALinkOneToOneSequential<File, IStrategoTerm> {

    @Override
    public Diff<IStrategoTerm> sinkWork(Diff<File> input) {
	return new Diff<IStrategoTerm>(ParseService.INSTANCE().parse(
		input.getPayload()), input.getDiffKind());
    }

}
