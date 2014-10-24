/**
 * 
 */
package org.metaborg.sunshine.services.parser;

import org.apache.commons.vfs2.FileObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metaborg.sunshine.environment.ServiceRegistry;
import org.metaborg.sunshine.pipeline.connectors.ALinkOneToOne;
import org.metaborg.sunshine.pipeline.diff.Diff;
import org.metaborg.sunshine.pipeline.diff.DiffKind;
import org.metaborg.sunshine.services.analyzer.AnalysisFileResult;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class JSGLRLink extends ALinkOneToOne<FileObject, AnalysisFileResult> {

	private static final Logger logger = LogManager.getLogger(JSGLRLink.class.getName());

	@Override
	public Diff<AnalysisFileResult> sinkWork(Diff<FileObject> input) {
		if (input.getDiffKind() == DiffKind.DELETION) {
			logger.debug("File {} has been deleted therefore has no AST", input.getPayload()
					.getName());
			return null;
		}
		AnalysisFileResult parseResult = ServiceRegistry.INSTANCE()
				.getService(ParserService.class).parseFile(input.getPayload());
		logger.trace("Parsing of file {} produced AST {} and {} messages", input.getPayload(),
				parseResult.ast(), parseResult.messages().size());
		return new Diff<AnalysisFileResult>(parseResult, input.getDiffKind());
	}

}
