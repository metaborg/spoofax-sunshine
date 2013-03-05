/**
 * 
 */
package org.spoofax.sunshine.framework.messages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 *
 */
public class PositionRegionTest {

	/**
	 * Test method for {@link org.spoofax.sunshine.framework.messages.PositionRegion#hashCode()}.
	 */
	@Test
	public void testHashCode1() {
		final PositionRegion reg1 = new PositionRegion(1, 2, 3, 4);
		final PositionRegion reg2 = new PositionRegion(1, 2, 3, 4);
		assertEquals(reg1.hashCode(), reg2.hashCode());
	}
	
	/**
	 * Test method for {@link org.spoofax.sunshine.framework.messages.PositionRegion#hashCode()}.
	 */
	@Test
	public void testHashCode2() {
		final PositionRegion reg1 = new PositionRegion(1, 2, 3, 4);
		final PositionRegion reg2 = new PositionRegion(1, 2, 3, 5);
		assertFalse(reg1.hashCode() == reg2.hashCode());
	}

	/**
	 * Test method for {@link org.spoofax.sunshine.framework.messages.PositionRegion#toString()}.
	 */
	@Test
	public void testToString() {
		final PositionRegion reg = new PositionRegion(1, 2, 3, 4);
		assertNotNull(reg.toString());
		assertTrue(reg.toString().length() > 0);
	}

	/**
	 * Test method for {@link org.spoofax.sunshine.framework.messages.PositionRegion#equals(java.lang.Object)}.
	 */
	@Test
	public void testEqualsObject1() {
		final PositionRegion reg1 = new PositionRegion(1, 2, 3, 4);
		final PositionRegion reg2 = new PositionRegion(1, 2, 3, 4);
		assertEquals(reg1, reg2);
	}

	/**
	 * Test method for {@link org.spoofax.sunshine.framework.messages.PositionRegion#equals(java.lang.Object)}.
	 */
	@Test
	public void testEqualsObject2() {
		final PositionRegion reg1 = new PositionRegion(1, 2, 3, 4);
		final PositionRegion reg2 = new PositionRegion(1, 2, 3, 5);
		assertFalse(reg1.equals(reg2));
	}
	
	/**
	 * Test method for {@link org.spoofax.sunshine.framework.messages.PositionRegion#PositionRegion(int, int, int, int)}.
	 */
	@Test
	public void testPositionRegion() {
		new PositionRegion(1,2,3,4);
	}

}
