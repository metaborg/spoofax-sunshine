/**
 * 
 */
package org.spoofax.sunshine.drivers;

import java.util.Collection;

import org.spoofax.sunshine.framework.messages.IMessage;
import org.spoofax.sunshine.framework.services.pipeline.servicewrappers.IPartition;
import org.spoofax.sunshine.framework.services.pipeline2.ILinkManyToMany;
import org.spoofax.sunshine.framework.services.pipeline2.ILinkOneToOne;
import org.spoofax.sunshine.framework.services.pipeline2.ISinkMany;
import org.spoofax.sunshine.framework.services.pipeline2.ISourceMany;
import org.spoofax.sunshine.framework.services.pipeline2.LinkMapperOneToOneSequential;

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

    /**
     * - take a directory to monitor => produces a list of files that need to be
     * analyzed
     * 
     * - parse all files => produce a map of filename and ast
     * 
     * - analyze all files => produce a map of filename and analysis result
     * 
     * - collect messages => produce a set of messages per file
     * 
     */

    public Collection<IMessage> assemble() {


	// files source
	ISourceMany<IPartition> filesSrc = null;

	// the parser
	ILinkOneToOne<IPartition, IParserResult> parserLink = null;
	// link to map the parser over the files

	LinkMapperOneToOneSequential<IPartition, IParserResult> parserMapper = new LinkMapperOneToOneSequential<IPartition, IParserResult>(
		parserLink);
	filesSrc.addSink(parserMapper);

	ILinkManyToMany<IParserResult, IAnalyzerResult> analyzerLink = null;
	parserMapper.addSink(analyzerLink);

	ISinkMany<IAnalyzerResult> messageSink = null;
	analyzerLink.addSink(messageSink);

	return null;
    }

    private interface IParserResult {
    }

    private interface IAnalyzerResult {
    }

}
