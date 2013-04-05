/**
 * 
 */
package org.spoofax.sunshine.model.messages;

import org.spoofax.jsglr.client.imploder.IToken;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class TokenRegion extends ARegion {

    private IToken left, right;

    public TokenRegion(IToken left, IToken right) {
	if (left == null || right == null) {
	    assert false;
	}
	this.left = left;
	this.right = right;
    }

    @Override
    public String toString() {
	final StringBuilder str = new StringBuilder();
	str.append(left.getLine());
	str.append(COMMA);
	str.append(left.getColumn());
	str.append(COLON);
	str.append(right.getEndLine());
	str.append(COMMA);
	str.append(right.getEndColumn());
	return str.toString();
    }

    @Override
    public boolean equals(Object o) {
	if (o instanceof TokenRegion) {
	    TokenRegion or = (TokenRegion) o;
	    return tokensEqual(left, or.left) && tokensEqual(right, or.right);
	}
	return false;
    }

    @Override
    public int hashCode() {
	final StringBuilder hashString = new StringBuilder();
	hashString.append(left.getLine());
	hashString.append(left.getColumn());
	hashString.append(right.getLine());
	hashString.append(right.getColumn());
	return hashString.toString().hashCode();
    }

    private boolean tokensEqual(IToken a, IToken b) {
	return a.getColumn() == b.getColumn() && a.getLine() == b.getLine()
		&& a.getEndColumn() == b.getEndColumn()
		&& a.getEndLine() == b.getEndLine();
    }

}
