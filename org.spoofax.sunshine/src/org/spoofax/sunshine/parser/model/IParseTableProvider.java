/**
 * 
 */
package org.spoofax.sunshine.parser.model;

import org.spoofax.jsglr.client.ParseTable;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public interface IParseTableProvider {

	ParseTable getParseTable();
}
