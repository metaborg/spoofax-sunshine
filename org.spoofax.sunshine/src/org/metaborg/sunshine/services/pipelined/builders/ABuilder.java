package org.metaborg.sunshine.services.pipelined.builders;

/**
 * A partial implementation of the {@link IBuilder} interface. Extending classes will have to
 * implement the {@link #invoke(org.spoofax.interpreter.terms.IStrategoTerm)} function.
 * 
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public abstract class ABuilder implements IBuilder {

	private final String name;
	private final boolean onSource;
	private final boolean meta;

	public ABuilder(String name, boolean onSource, boolean meta) {
		this.name = name;
		this.onSource = onSource;
		this.meta = meta;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isOnSource() {
		return onSource;
	}

	@Override
	public boolean isMeta() {
		return meta;
	}

	@Override
	public String toString() {
		return "Builder " + name + " on source " + onSource + " and meta " + meta;
	}

}
