/**
 * 
 */
package org.spoofax.sunshine.parser.model;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class ParserConfig implements IParserConfig {

	private final String startSymbol;
	private final IParseTableProvider parseTableProvider;
	private final int timeout;

	public ParserConfig(String startSymbol, IParseTableProvider provider, int timeout) {
		this.startSymbol = startSymbol;
		this.parseTableProvider = provider;
		this.timeout = timeout;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.spoofax.sunshine.parser.framework.IParserConfig#getStartSymbol()
	 */
	@Override
	public String getStartSymbol() {
		return this.startSymbol;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.spoofax.sunshine.parser.framework.IParserConfig#getParseTableProvider ()
	 */
	@Override
	public IParseTableProvider getParseTableProvider() {
		return this.parseTableProvider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.spoofax.sunshine.parser.framework.IParserConfig#getTimeout()
	 */
	@Override
	public int getTimeout() {
		return this.timeout;
	}
}
