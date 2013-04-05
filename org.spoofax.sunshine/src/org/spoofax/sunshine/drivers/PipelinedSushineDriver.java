/**
 * 
 */
package org.spoofax.sunshine.drivers;

import java.io.File;
import java.util.Collection;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.sunshine.framework.messages.IMessage;
import org.spoofax.sunshine.framework.services.pipeline.ILinkManyToMany;
import org.spoofax.sunshine.framework.services.pipeline.ILinkOneToOne;
import org.spoofax.sunshine.framework.services.pipeline.ISourceMany;
import org.spoofax.sunshine.framework.services.pipeline.LinkMapperOneToOneSequential;
import org.spoofax.sunshine.framework.services.pipeline.servicewrappers.AnalyzerLink;
import org.spoofax.sunshine.framework.services.pipeline.servicewrappers.FileSource;
import org.spoofax.sunshine.framework.services.pipeline.servicewrappers.ParserLink;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class PipelinedSushineDriver {

    /**
     * @param args
     */
    public static void main(String[] args) {
	// TODO Auto-generated method stub

    }

    public Collection<IMessage> assemble() {

	// files source
	ISourceMany<File> filesSrc = new FileSource();

	// the parser
	ILinkOneToOne<File, IStrategoTerm> parserLink = new ParserLink();

	// link to map the parser over the files
	LinkMapperOneToOneSequential<File, IStrategoTerm> parserMapper = new LinkMapperOneToOneSequential<File, IStrategoTerm>(
		parserLink);
	filesSrc.addSink(parserMapper);

	// link the analyzer to work on the files
	ILinkManyToMany<File, IStrategoTerm> analyzerLink = new AnalyzerLink();
	filesSrc.addSink(analyzerLink);

	return null;
    }

}
