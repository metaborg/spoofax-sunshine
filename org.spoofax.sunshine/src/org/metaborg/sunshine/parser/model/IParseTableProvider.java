/**
 * 
 */
package org.metaborg.sunshine.parser.model;

import java.nio.file.Path;

import org.spoofax.jsglr.client.ParseTable;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public interface IParseTableProvider {

	ParseTable getParseTable();

	Path getPathToParseTable();
}
