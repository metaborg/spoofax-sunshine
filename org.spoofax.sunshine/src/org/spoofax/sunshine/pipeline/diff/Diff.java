/**
 * 
 */
package org.spoofax.sunshine.pipeline.diff;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class Diff<T> {
	private final T payload;
	private final DiffKind diffKind;

	public Diff(T payload, DiffKind diffKind) {
		this.payload = payload;
		this.diffKind = diffKind;
	}

	public T getPayload() {
		return payload;
	}

	public DiffKind getDiffKind() {
		return diffKind;
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return super.hashCode();
	}

	@Override
	public String toString() {
		return diffKind + " " + payload;
	}
}
