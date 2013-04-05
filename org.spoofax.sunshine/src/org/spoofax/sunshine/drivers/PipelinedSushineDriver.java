/**
 * 
 */
package org.spoofax.sunshine.drivers;

import java.io.File;
import java.util.Collection;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.sunshine.model.messages.IMessage;
import org.spoofax.sunshine.parser.model.IParseResult;
import org.spoofax.sunshine.pipeline.ILinkManyToMany;
import org.spoofax.sunshine.pipeline.ILinkOneToOne;
import org.spoofax.sunshine.pipeline.ISourceMany;
import org.spoofax.sunshine.pipeline.LinkMapperOneToOne;
import org.spoofax.sunshine.pipeline.services.AnalyzerLink;
import org.spoofax.sunshine.pipeline.services.FileSource;
import org.spoofax.sunshine.pipeline.services.JSGLRLink;
import org.spoofax.sunshine.services.analysis.IAnalysisResult;

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
	ILinkOneToOne<File, IParseResult<IStrategoTerm>> parserLink = new JSGLRLink();

	// link to map the parser over the files
	LinkMapperOneToOne<File, IParseResult<IStrategoTerm>> parserMapper = new LinkMapperOneToOne<File, IParseResult<IStrategoTerm>>(
		parserLink);
	filesSrc.addSink(parserMapper);

	// link the analyzer to work on the files
	ILinkManyToMany<File, IAnalysisResult> analyzerLink = new AnalyzerLink();
	filesSrc.addSink(analyzerLink);

	// TODO link ONE message collector to both the analyzer and the parser
	// and report the messages

	return null;
    }

}
