package org.spoofax.sunshine.parser.jsglr;

import java.util.Collection;
import java.util.LinkedList;
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
import org.spoofax.sunshine.messages.IMessage;
import org.spoofax.sunshine.messages.Message;
import org.spoofax.sunshine.messages.MessageSeverity;
import org.spoofax.sunshine.messages.MessageType;
import org.spoofax.sunshine.parser.framework.ASGLRI;
import org.spoofax.sunshine.parser.framework.IParserConfig;
import org.spoofax.sunshine.parser.framework.ParserException;
import org.spoofax.terms.attachments.ParentTermFactory;

/**
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 */
public class JSGLRI extends ASGLRI {

	private boolean useRecovery = false;

	private SGLR parser;

	private Disambiguator disambiguator;

	private int cursorLocation = Integer.MAX_VALUE;

	private final Collection<IMessage> messages = new LinkedList<IMessage>();

	// Initialization and parsing

	public void setCursorLocation(int cursorLocation) {
		this.cursorLocation = cursorLocation;
	}

	public JSGLRI(IParserConfig config) throws ParserException {
		this.config = config;
		final TermTreeFactory factory = new TermTreeFactory(new ParentTermFactory(Environment.INSTANCE().termFactory));
		this.parser = new SGLR(new TreeBuilder(factory), config.getParseTableProvider().getParseTable());
		resetState();
	}

	// public SGLR getParser() {
	// return parser;
	// }

	/**
	 * @see SGLR#setUseStructureRecovery(boolean)
	 */
	public void setUseRecovery(boolean useRecovery) {
		this.useRecovery = useRecovery;
		parser.setUseStructureRecovery(useRecovery);
	}

	// public Disambiguator getDisambiguator() {
	// return disambiguator;
	// }
	//
	// public void setDisambiguator(Disambiguator disambiguator) {
	// this.disambiguator = disambiguator;
	// }

	/**
	 * Resets the state of this parser, reinitializing the SGLR instance
	 */
	void resetState() {
		parser.setTimeout(config.getTimeout());
		if (disambiguator != null)
			parser.setDisambiguator(disambiguator);
		else
			disambiguator = parser.getDisambiguator();
		setUseRecovery(useRecovery);
		if (!isImplodeEnabled()) {
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
		ParserException toThrow = null;
		messages.clear();
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
			toThrow = new ParserException(e);
		} finally {
			// TODO proper message collection
			Set<BadTokenException> errors = parser.getCollectedErrors();
			for (BadTokenException badTokenException : errors) {
				final Message msg = new Message();
				msg.type = MessageType.PARSER_MESSAGE;
				msg.severity = MessageSeverity.ERROR;
				msg.file = filename;
				msg.msg = badTokenException.getMessage();
				messages.add(msg);
			}
		}

		if (toThrow != null) {
			throw toThrow;
		}

		return result;
	}

	@Override
	public Collection<IMessage> getMessages() {
		return new LinkedList<IMessage>(messages);
	}

}
