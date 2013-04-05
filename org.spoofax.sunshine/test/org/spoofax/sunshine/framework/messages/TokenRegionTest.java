/**
 * 
 */
package org.spoofax.sunshine.framework.messages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.spoofax.interpreter.terms.ISimpleTerm;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ITokenizer;
import org.spoofax.sunshine.model.messages.TokenRegion;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class TokenRegionTest {

    /**
     * Test method for
     * {@link org.spoofax.sunshine.model.messages.TokenRegion#hashCode()}.
     */
    @Test
    public void testHashCode1() {
	final TokenRegion reg1 = new TokenRegion(tok1, tok2);
	final TokenRegion reg2 = new TokenRegion(tok1, tok2);
	assertEquals(reg1.hashCode(), reg2.hashCode());
    }

    /**
     * Test method for
     * {@link org.spoofax.sunshine.model.messages.TokenRegion#hashCode()}.
     */
    @Test
    public void testHashCode2() {
	final TokenRegion reg1 = new TokenRegion(tok1, tok2);
	final TokenRegion reg2 = new TokenRegion(tok2, tok1);
	assertFalse(reg1.hashCode() == reg2.hashCode());
    }

    /**
     * Test method for
     * {@link org.spoofax.sunshine.model.messages.TokenRegion#toString()}.
     */
    @Test
    public void testToString() {
	final TokenRegion reg1 = new TokenRegion(tok1, tok2);
	assertNotNull(reg1.toString());
	assertTrue(reg1.toString().length() > 0);
    }

    /**
     * Test method for
     * {@link org.spoofax.sunshine.model.messages.TokenRegion#equals(java.lang.Object)}
     * .
     */
    @Test
    public void testEqualsObject1() {
	final TokenRegion reg1 = new TokenRegion(tok1, tok2);
	final TokenRegion reg2 = new TokenRegion(tok1, tok2);
	assertEquals(reg1, reg2);
    }

    /**
     * Test method for
     * {@link org.spoofax.sunshine.model.messages.TokenRegion#equals(java.lang.Object)}
     * .
     */
    @Test
    public void testEqualsObject2() {
	final TokenRegion reg1 = new TokenRegion(tok1, tok2);
	final TokenRegion reg2 = new TokenRegion(tok2, tok1);
	assertFalse(reg1.equals(reg2));
    }

    /**
     * Test method for
     * {@link org.spoofax.sunshine.model.messages.TokenRegion#TokenRegion(org.spoofax.jsglr.client.imploder.IToken, org.spoofax.jsglr.client.imploder.IToken)}
     * .
     */
    @Test(expected = AssertionError.class)
    public void testTokenRegion1() {
	new TokenRegion(null, null);
    }

    /**
     * Test method for
     * {@link org.spoofax.sunshine.model.messages.TokenRegion#TokenRegion(org.spoofax.jsglr.client.imploder.IToken, org.spoofax.jsglr.client.imploder.IToken)}
     * .
     */
    @Test
    public void testTokenRegion2() {
	new TokenRegion(tok1, tok2);
    }

    final IToken tok1 = new IToken() {
	/**
		 * 
		 */
	private static final long serialVersionUID = -3952104732492908939L;

	@Override
	public IToken clone() {
	    return null;
	}

	@Override
	public int compareTo(IToken o) {
	    return 0;
	}

	@Override
	public void setKind(int arg0) {

	}

	@Override
	public ITokenizer getTokenizer() {
	    return null;
	}

	@Override
	public int getStartOffset() {
	    return 0;
	}

	@Override
	public int getLine() {
	    return 55;
	}

	@Override
	public int getLength() {
	    return 0;
	}

	@Override
	public int getKind() {
	    return 0;
	}

	@Override
	public int getIndex() {
	    return 0;
	}

	@Override
	public String getError() {
	    // TODO Auto-generated method stub
	    return null;
	}

	@Override
	public int getEndOffset() {
	    return 0;
	}

	@Override
	public int getEndLine() {
	    return 7;
	}

	@Override
	public int getEndColumn() {
	    return 5;
	}

	@Override
	public int getColumn() {
	    return 1;
	}

	@Override
	public ISimpleTerm getAstNode() {
	    return null;
	}

	@Override
	public char charAt(int arg0) {
	    return 43;
	}
    };

    final IToken tok2 = new IToken() {
	/**
		 * 
		 */
	private static final long serialVersionUID = -3952104732492908938L;

	@Override
	public IToken clone() {
	    return null;
	}

	@Override
	public int compareTo(IToken o) {
	    return 0;
	}

	@Override
	public void setKind(int arg0) {

	}

	@Override
	public ITokenizer getTokenizer() {
	    return null;
	}

	@Override
	public int getStartOffset() {
	    return 0;
	}

	@Override
	public int getLine() {
	    return 1;
	}

	@Override
	public int getLength() {
	    return 0;
	}

	@Override
	public int getKind() {
	    return 0;
	}

	@Override
	public int getIndex() {
	    return 0;
	}

	@Override
	public String getError() {
	    return null;
	}

	@Override
	public int getEndOffset() {
	    return 0;
	}

	@Override
	public int getEndLine() {
	    return 2;
	}

	@Override
	public int getEndColumn() {
	    return 2;
	}

	@Override
	public int getColumn() {
	    return 1;
	}

	@Override
	public ISimpleTerm getAstNode() {
	    return null;
	}

	@Override
	public char charAt(int arg0) {
	    return 42;
	}
    };
}
