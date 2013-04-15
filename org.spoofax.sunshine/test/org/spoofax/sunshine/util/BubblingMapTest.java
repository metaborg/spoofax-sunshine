package org.spoofax.sunshine.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BubblingMapTest {

    private Map<String, String> top;
    private Map<String, String> bottom;
    private BubblingMap<String, String> bmap;

    @Before
    public void setUp() throws Exception {
	this.top = new HashMap<String, String>();
	this.bottom = new HashMap<String, String>();
	this.bmap = new BubblingMap<String, String>(bottom, top);
    }

    @After
    public void tearDown() throws Exception {
	this.top = null;
	this.bottom = null;
	this.bmap = null;
    }

    @Test
    public void testBubblingMap1() {
	BubblingMap<String, String> map = new BubblingMap<String, String>(
		bottom, top);
	assertEquals(top, map.top());
	assertEquals(bottom, map.bottom());
    }

    @Test
    public void testBubblingMap2() {
	BubblingMap<String, String> map = new BubblingMap<String, String>(null,
		top);
	assertEquals(top, map.top());
	assertEquals(null, map.bottom());
    }

    @Test
    public void testBubblingMap3() {
	BubblingMap<String, String> map = new BubblingMap<String, String>(
		bottom, null);
	assertEquals(null, map.top());
	assertEquals(bottom, map.bottom());
    }

    @Test
    public void testEntrySet1() {
	assertEquals(0, bmap.entrySet().size());
    }

    @Test
    public void testEntrySet2() {
	bottom.put("one", "two");
	assertEquals(1, bmap.entrySet().size());
    }

    @Test
    public void testEntrySet3() {
	bottom.put("one", "two");
	top.put("two", "three");
	assertEquals(2, bmap.entrySet().size());
    }

    @Test
    public void testEntrySet4() {
	bottom.put("one", "two");
	bmap.put("one", "foobar");
	assertEquals(1, bmap.entrySet().size());
    }

    @Test
    public void testEntrySet5() {
	bottom.put("one", "two");
	bottom.put("two", "three");
	bottom.put("three", "four");
	bmap.put("one", "42");
	bmap.put("two", "69");
	boolean one = false, two = false, three = false;
	Iterator<Entry<String, String>> iter = bmap.entrySet().iterator();
	while (iter.hasNext()) {
	    Entry<String, String> entry = iter.next();
	    if (entry.getKey() == "one") {
		assertFalse(one);
		assertEquals("42", entry.getValue());
		one = true;
	    }
	    if (entry.getKey() == "two") {
		assertFalse(two);
		assertEquals("69", entry.getValue());
		two = true;
	    }
	    if (entry.getKey() == "three") {
		assertFalse(three);
		assertEquals("four", entry.getValue());
		three = true;
	    }

	}
	assertTrue(one);
	assertTrue(two);
	assertTrue(three);
    }

    @Test
    public void testEntrySet6() {
	bottom.put("one", "two");
	bottom.put("two", "three");
	bottom.put("three", "four");
	bmap.put("one", "42");
	bmap.put("two", "69");
	assertEquals(3, bmap.entrySet().size());
	assertEquals(2, top.entrySet().size());
	assertEquals(1, bottom.entrySet().size());
	Iterator<Entry<String, String>> iter = bmap.entrySet().iterator();
	assertNotNull(iter.next());
	assertNotNull(iter.next());
	assertNotNull(iter.next());
    }

    @Test(expected = NoSuchElementException.class)
    public void testEntrySet7() {
	bottom.put("one", "two");
	bottom.put("two", "three");
	bottom.put("three", "four");
	bmap.put("one", "42");
	bmap.put("two", "69");
	Iterator<Entry<String, String>> iter = bmap.entrySet().iterator();
	assertNotNull(iter.next());
	assertNotNull(iter.next());
	assertNotNull(iter.next());
	iter.next();
    }

    @Test(expected = IllegalStateException.class)
    public void testEntrySet8() {
	bottom.put("one", "two");
	bottom.put("two", "three");
	bottom.put("three", "four");
	bmap.put("one", "42");
	bmap.put("two", "69");
	Iterator<Entry<String, String>> iter = bmap.entrySet().iterator();
	iter.remove();
    }

    @Test
    public void testEntrySet9() {
	bottom.put("one", "two");
	bottom.put("two", "three");
	bottom.put("three", "four");
	bmap.put("one", "42");
	bmap.put("two", "69");
	assertEquals(3, bmap.entrySet().size());
	assertEquals(2, top.entrySet().size());
	assertEquals(1, bottom.entrySet().size());
	Iterator<Entry<String, String>> iter = bmap.entrySet().iterator();
	iter.next();
	iter.remove();
	assertEquals(2, bmap.entrySet().size());
    }

    @Test(expected = IllegalStateException.class)
    public void testEntrySet10() {
	bottom.put("one", "two");
	bottom.put("two", "three");
	bottom.put("three", "four");
	bmap.put("one", "42");
	bmap.put("two", "69");
	Iterator<Entry<String, String>> iter = bmap.entrySet().iterator();
	iter.next();
	iter.remove();
	iter.remove();
    }

    @Test
    public void testEntrySet11() {
	bottom.put("one", "two");
	bottom.put("two", "three");
	bottom.put("three", "four");
	bmap.put("one", "42");
	bmap.put("two", "69");
	assertEquals(3, bmap.entrySet().size());
	assertEquals(2, top.entrySet().size());
	assertEquals(1, bottom.entrySet().size());
	Iterator<Entry<String, String>> iter = bmap.entrySet().iterator();
	assertTrue(iter.hasNext());
	iter.next();
	assertTrue(iter.hasNext());
	iter.next();
	assertTrue(iter.hasNext());
	iter.next();
	assertFalse(iter.hasNext());
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testEntrySet12() {
	bottom.put("one", "two");
	bottom.put("two", "three");
	bottom.put("three", "four");
	bmap.put("one", "42");
	bmap.put("two", "69");
	assertEquals(3, bmap.entrySet().size());
	assertEquals(2, top.entrySet().size());
	assertEquals(1, bottom.entrySet().size());
	Iterator<Entry<String, String>> iter = bmap.entrySet().iterator();
	bmap.dropBottom();
	iter.hasNext();
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testEntrySet13() {
	bottom.put("one", "two");
	bottom.put("two", "three");
	bottom.put("three", "four");
	bmap.put("one", "42");
	bmap.put("two", "69");
	assertEquals(3, bmap.entrySet().size());
	assertEquals(2, top.entrySet().size());
	assertEquals(1, bottom.entrySet().size());
	Iterator<Entry<String, String>> iter = bmap.entrySet().iterator();
	bmap.dropBottom();
	iter.next();
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testEntrySet14() {
	bottom.put("one", "two");
	bottom.put("two", "three");
	bottom.put("three", "four");
	bmap.put("one", "42");
	bmap.put("two", "69");
	assertEquals(3, bmap.entrySet().size());
	assertEquals(2, top.entrySet().size());
	assertEquals(1, bottom.entrySet().size());
	Iterator<Entry<String, String>> iter = bmap.entrySet().iterator();
	iter.next();
	bmap.dropBottom();
	iter.remove();
    }

    @Test
    public void testPutKV1() {
	assertNull(bottom.get("hello"));
	bmap.put("hello", "world");
	assertEquals("world", top.get("hello"));
	assertNull(bottom.get("hello"));
    }

    @Test
    public void testPutKV2() {
	bottom.put("hello", "world");
	bmap.put("hello", "world");
	assertEquals("world", top.get("hello"));
	assertNull(bottom.get("hello"));
    }

    @Test
    public void testPutKV3() {
	bottom.put("hello", "jumbo");
	bmap.put("hello", "world");
	assertEquals("world", top.get("hello"));
	assertNull(bottom.get("hello"));
    }

    @Test
    public void testPutKV4() {
	bottom.put("hello", "jumbo");
	bottom.put("one", "fool");
	bmap.put("hello", "world");
	assertEquals("world", top.get("hello"));
	assertEquals("world", bmap.get("hello"));
	assertNull(bottom.get("hello"));
	assertEquals("fool", bmap.get("one"));
	assertEquals("fool", bottom.get("one"));
	assertNull(top.get("one"));
    }

    @Test
    public void testPutKV5() {
	bottom.put("hello", "jumbo");
	bottom.put("one", "fool");
	bmap.put("hello", "world");
	assertEquals(1, top.entrySet().size());
	assertEquals(1, bottom.entrySet().size());
    }

    @Test
    public void testRemoveObject1() {
	bottom.put("hello", "world");
	bmap.remove("hello");
	assertNull(bottom.get("hello"));
	assertNull(bmap.get("hello"));
    }

    @Test
    public void testRemoveObject2() {
	top.put("hello", "world");
	bmap.remove("hello");
	assertNull(top.get("hello"));
	assertNull(bmap.get("hello"));
    }

    @Test
    public void testRemoveObject3() {
	bottom.put("hello", "jumbo");
	bmap.put("hello", "world");
	assertEquals("world", bmap.get("hello"));
	bmap.remove("hello");
	assertNull(bmap.get("hello"));
	assertNull(top.get("hello"));
	assertNull(bottom.get("hello"));
    }

    @Test
    public void testTop() {
	assertEquals(top, bmap.top());
    }

    @Test
    public void testBottom() {
	assertEquals(bottom, bmap.bottom());
    }

    @Test
    public void testDropBottom1() {
	bmap.dropBottom();
	assertEquals(null, bmap.bottom());
    }

    @Test
    public void testDropBottom2() {
	bottom.put("hello", "world");
	assertEquals("world", bmap.get("hello"));
	bmap.dropBottom();
	assertEquals(null, bmap.get("hello"));
    }

    @Test
    public void testNoBottom1() {
	bmap.dropBottom();
	assertEquals(0, bmap.size());
    }

    @Test
    public void testNoBottom2() {
	bmap.dropBottom();
	bmap.put("a", "aa");
	assertEquals(1, bmap.size());
    }

    @Test
    public void testNoBottom3() {
	bmap.put("a", "aa");
	bmap.dropBottom();
	assertEquals(1, bmap.size());
	bmap.remove("a");
	assertEquals(0, bmap.size());
    }

    @Test
    public void testNoBottom4() {
	bottom.put("a", "aa");
	bmap.put("b", "bb");
	bmap.dropBottom();
	assertEquals("bb", bmap.get("b"));
	assertNull(bmap.get("a"));
    }

    @Test
    public void testNoBottom5() {
	bottom.put("a", "aa");
	bottom.put("b", "bb");
	bmap.put("c", "ccc");
	bmap.put("b", "bbb");
	bmap.dropBottom();
	assertEquals(2, bmap.size());
    }

    @Test
    public void testNoBottom6() {
	bmap.dropBottom();
	bmap.remove("foo");
    }

    @Test
    public void testNoBottom7() {
	bmap.put("bar", "bar");
	bmap.dropBottom();
	bmap.remove("foo");
    }

}
