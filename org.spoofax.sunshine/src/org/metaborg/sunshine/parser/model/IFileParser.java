/**
 * 
 */
package org.metaborg.sunshine.parser.model;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.sunshine.services.analyzer.AnalysisFileResult;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public interface IFileParser<T> {
	AnalysisFileResult parse();

	IParserConfig getConfig();

	FileObject getFile();
}
