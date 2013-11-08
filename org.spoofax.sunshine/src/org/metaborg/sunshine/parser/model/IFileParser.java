/**
 * 
 */
package org.metaborg.sunshine.parser.model;

import java.io.File;

import org.metaborg.sunshine.services.analyzer.AnalysisResult;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public interface IFileParser<T> {

	AnalysisResult parse();

	IParserConfig getConfig();

	File getFile();
}
