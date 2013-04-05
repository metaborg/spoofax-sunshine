/**
 * 
 */
package org.spoofax.sunshine.drivers;

import java.io.File;
import java.util.Collection;

import org.spoofax.sunshine.model.messages.IMessage;
import org.spoofax.sunshine.parser.model.IStrategoParseOrAnalyzeResult;
import org.spoofax.sunshine.pipeline.ILinkManyToMany;
import org.spoofax.sunshine.pipeline.ILinkOneToOne;
import org.spoofax.sunshine.pipeline.ISourceMany;
import org.spoofax.sunshine.pipeline.LinkMapperOneToOne;
import org.spoofax.sunshine.pipeline.services.AnalyzerLink;
import org.spoofax.sunshine.pipeline.services.FileSource;
import org.spoofax.sunshine.pipeline.services.JSGLRLink;
import org.spoofax.sunshine.pipeline.services.MessageExtractorLink;
import org.spoofax.sunshine.pipeline.services.MessageSink;

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
	ILinkOneToOne<File, IStrategoParseOrAnalyzeResult> parserLink = new JSGLRLink();

	// link to map the parser over the files
	LinkMapperOneToOne<File, IStrategoParseOrAnalyzeResult> parserMapper = new LinkMapperOneToOne<File, IStrategoParseOrAnalyzeResult>(
		parserLink);
	filesSrc.addSink(parserMapper);

	// link the analyzer to work on the files
	ILinkManyToMany<File, IStrategoParseOrAnalyzeResult> analyzerLink = new AnalyzerLink();
	filesSrc.addSink(analyzerLink);

	// create a Parser and Analyzer message extractor
	ILinkManyToMany<IStrategoParseOrAnalyzeResult, IMessage> messageSelector = new MessageExtractorLink();
	parserMapper.addSink(messageSelector);
	analyzerLink.addSink(messageSelector);

	// create a single message sink
	MessageSink messageSink = new MessageSink();
	messageSelector.addSink(messageSink);

	return null;
    }

}
