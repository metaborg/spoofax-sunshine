package org.spoofax.sunshine.parser.impl;

import java.util.Set;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.Asfix2TreeBuilder;
import org.spoofax.jsglr.client.Disambiguator;
import org.spoofax.jsglr.client.FilterException;
import org.spoofax.jsglr.client.imploder.ITreeFactory;
import org.spoofax.jsglr.client.imploder.TermTreeFactory;
import org.spoofax.jsglr.client.imploder.TreeBuilder;
import org.spoofax.jsglr.io.SGLR;
import org.spoofax.jsglr.shared.BadTokenException;
import org.spoofax.jsglr.shared.SGLRException;
import org.spoofax.sunshine.Environment;
import org.spoofax.sunshine.parser.framework.AParser;
import org.spoofax.sunshine.parser.framework.IParserConfig;
import org.spoofax.sunshine.parser.framework.ParserException;
import org.spoofax.terms.attachments.ParentTermFactory;

/**
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 */
public class JSGLRI extends AParser {

	private boolean useRecovery = false;

	private SGLR parser;

	private Disambiguator disambiguator;

	private int cursorLocation = Integer.MAX_VALUE;

	private boolean implodeEnabled = true;

	// Initialization and parsing

	public void setCursorLocation(int cursorLocation) {
		this.cursorLocation = cursorLocation;
	}

	public JSGLRI(IParserConfig config) throws ParserException {
		super(config);
		final TermTreeFactory factory = new TermTreeFactory(new ParentTermFactory(Environment.INSTANCE().termFactory));
		this.parser = new SGLR(new TreeBuilder(factory), config.getParseTableProvider().getParseTable());
		resetState();
	}

	public void setUseRecovery(boolean useRecovery) {
		this.useRecovery = useRecovery;
		parser.setUseStructureRecovery(useRecovery);
	}

	public void setImplodeEnabled(boolean implode) {
		this.implodeEnabled = implode;
		resetState();
	}

	/**
	 * Resets the state of this parser, reinitializing the SGLR instance
	 */
	private void resetState() {
		parser.setTimeout(config.getTimeout());
		if (disambiguator != null)
			parser.setDisambiguator(disambiguator);
		else
			disambiguator = parser.getDisambiguator();
		setUseRecovery(useRecovery);
		if (!implodeEnabled) {
			parser.setTreeBuilder(new Asfix2TreeBuilder(Environment.INSTANCE().termFactory));
		} else {
			assert parser.getTreeBuilder() instanceof TreeBuilder;
			@SuppressWarnings("unchecked")
			ITreeFactory<IStrategoTerm> treeFactory = ((TreeBuilder) parser.getTreeBuilder()).getFactory();
			assert ((TermTreeFactory) treeFactory).getOriginalTermFactory() instanceof ParentTermFactory;
		}
	}

	@Override
	protected IStrategoTerm doParse(String input, String filename) throws ParserException {
		IStrategoTerm result = null;
		try {
			try {
				result = (IStrategoTerm) parser.parse(input, filename, config.getStartSymbol(), true, cursorLocation);
			} catch (FilterException e) {
				if (e.getCause() == null && parser.getDisambiguator().getFilterPriorities()) {
					disambiguator.setFilterPriorities(false);
					try {
						result = (IStrategoTerm) parser.parse(input, filename, config.getStartSymbol());
					} finally {
						disambiguator.setFilterPriorities(true);
					}
				} else {
					throw e;
				}
			}
		} catch (SGLRException e) {
			throw new ParserException(e);
		}catch (InterruptedException e) {
			throw new ParserException(e);
		}

		return result;
	}

	public Set<BadTokenException> getCollectedErrors() {
		return parser.getCollectedErrors();
	}

}
