/**
 * 
 */
package org.metaborg.sunshine.ant;

import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.types.Path;

/**
 * @author vladvergu
 * 
 */
public class DependencyAntType {
	// <dependency of="syntax/*.sdf" on="syntax/*.sdf3" vialanguage="SDF3"
	// viatransformation="generate-sdf" />

	public DependencyAntType() {

	}

	Path of;
	Path on;
	Path enterat;
	String vialanguage;
	String viabuilder;
	List<WithArgAntType> args = new ArrayList<>();

	public Path getOf() {
		return of;
	}

	public void setOf(Path of) {
		this.of = of;
	}

	public Path getOn() {
		return on;
	}

	public void setOn(Path on) {
		this.on = on;
	}

	public String getVialanguage() {
		return vialanguage;
	}

	public void setVialanguage(String vialanguage) {
		this.vialanguage = vialanguage;
	}

	public String getViabuilder() {
		return viabuilder;
	}

	public void setViabuilder(String viatransformation) {
		this.viabuilder = viatransformation;
	}

	public void setEnterat(Path enterat) {
		this.enterat = enterat;
	}

	public Path getEnterat() {
		return enterat;
	}

	public void add(WithArgAntType arg) {
		this.args.add(arg);
	}

	public List<WithArgAntType> getArgs() {
		return args;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("<dependency of=\"");
		builder.append(of);
		builder.append("\" on=\"");
		builder.append(on);
		builder.append("\" enterat=\"");
		builder.append(enterat);
		builder.append("\" vialanguage=\"");
		builder.append(vialanguage);
		builder.append("\" viabuilder=\"");
		builder.append(viabuilder);
		builder.append("\">\n");
		for (WithArgAntType arg : args) {
			builder.append("\t" + arg + "\n");
		}
		builder.append("</dependency>\n");
		return builder.toString();
	}

}
