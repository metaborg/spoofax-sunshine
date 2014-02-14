/**
 * 
 */
package org.metaborg.sunshine.ant;

/**
 * @author vladvergu
 * 
 */
public class WithArgAntType {

	String arg, value;

	public WithArgAntType() {
		throw new RuntimeException("Witharguments are currently not supported");
	}

	public String getArg() {
		return arg;
	}

	public void setArg(String arg) {
		this.arg = arg;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "<witharg name=\"" + arg + "\" value=\"" + value + "\"/>";
	}

}
