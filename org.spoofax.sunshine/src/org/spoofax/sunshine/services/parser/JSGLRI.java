package org.spoofax.sunshine.services.parser;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.Asfix2TreeBuilder;
import org.spoofax.jsglr.client.Disambiguator;
import org.spoofax.jsglr.client.FilterException;
import org.spoofax.jsglr.client.imploder.ITokenizer;
import org.spoofax.jsglr.client.imploder.ITreeFactory;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.jsglr.client.imploder.NullTokenizer;
import org.spoofax.jsglr.client.imploder.TermTreeFactory;
import org.spoofax.jsglr.client.imploder.TreeBuilder;
import org.spoofax.jsglr.io.SGLR;
import org.spoofax.jsglr.shared.SGLRException;
import org.spoofax.sunshine.CompilerException;
import org.spoofax.sunshine.Environment;
import org.spoofax.sunshine.parser.model.IFileParser;
import org.spoofax.sunshine.parser.model.IParserConfig;
import org.spoofax.sunshine.services.analyzer.AnalysisResult;
import org.spoofax.terms.attachments.ParentTermFactory;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 */
public class JSGLRI implements IFileParser<IStrategoTerm> {

	private final IParserConfig config;

	private boolean useRecovery = false;

	private SGLR parser;

	private Disambiguator disambiguator;

	private int cursorLocation = Integer.MAX_VALUE;

	private boolean implodeEnabled = true;

	private ITokenizer currentTokenizer;

	private final JSGLRParseErrorHandler errorHandler;

	private File file;

	public void setCursorLocation(int cursorLocation) {
		this.cursorLocation = cursorLocation;
	}

	public JSGLRI(IParserConfig config, File file) {
		assert config != null;
		this.config = config;
		final TermTreeFactory factory = new TermTreeFactory(new ParentTermFactory(
				Environment.INSTANCE().termFactory));
		this.parser = new SGLR(new TreeBuilder(factory), config.getParseTableProvider()
				.getParseTable());
		this.errorHandler = new JSGLRParseErrorHandler(this);
		assert file != null;
		this.file = file;
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
			ITreeFactory<IStrategoTerm> treeFactory = ((TreeBuilder) parser.getTreeBuilder())
					.getFactory();
			assert ((TermTreeFactory) treeFactory).getOriginalTermFactory() instanceof ParentTermFactory;
		}
	}

	@Override
	public AnalysisResult parse() {
		assert file != null;
		String input;
		try {
			input = FileUtils.readFileToString(file);
		} catch (IOException e) {
			throw new CompilerException("Could not read file", e);
		}

		assert input != null;

		IStrategoTerm ast = null;

		errorHandler.reset();
		currentTokenizer = new NullTokenizer(input, file.getName());
		try {
			ast = actuallyParse(input, file.getName());
			SourceAttachment.putSource(ast, file, config);
		} catch (Exception e) {
			errorHandler.setRecoveryFailed(true);
			errorHandler.gatherException(currentTokenizer, e);
		}

		if (ast != null) {
			currentTokenizer = ImploderAttachment.getTokenizer(ast);
			errorHandler.setRecoveryFailed(false);
			errorHandler.gatherNonFatalErrors(ast);
		}

		// result.setMessages(errorHandler.getCollectedMessages());
		return new AnalysisResult(null, file, errorHandler.getCollectedMessages(), ast);
	}

	private IStrategoTerm actuallyParse(String input, String filename) throws SGLRException,
			InterruptedException {
		IStrategoTerm result;
		try {
			result = (IStrategoTerm) parser.parse(input, filename, config.getStartSymbol(), true,
					cursorLocation);
		} catch (FilterException fex) {
			if (fex.getCause() == null && parser.getDisambiguator().getFilterPriorities()) {
				disambiguator.setFilterPriorities(false);
				try {
					result = (IStrategoTerm) parser.parse(input, filename, config.getStartSymbol());
				} finally {
					disambiguator.setFilterPriorities(true);
				}
			} else {
				throw fex;
			}
		}
		return result;
	}

	@Override
	public IParserConfig getConfig() {
		return config;
	}

	@Override
	public File getFile() {
		return file;
	}

	protected SGLR getParser() {
		return parser;
	}

}
