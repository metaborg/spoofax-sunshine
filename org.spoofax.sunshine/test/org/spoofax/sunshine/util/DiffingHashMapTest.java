package org.spoofax.sunshine.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Map.Entry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spoofax.sunshine.pipeline.diff.DiffKind;

public class DiffingHashMapTest {

    private DiffingHashMap<String, String> dmap;

    @Before
    public void setUp() throws Exception {
	dmap = new DiffingHashMap<String, String>();
    }

    @After
    public void tearDown() throws Exception {
	dmap = null;
    }

    @Test
    public void testDiffingHashMap() {
	new DiffingHashMap<String, String>();
    }

    @Test
    public void testBeginDiff1() {
	dmap.beginDiff();
	dmap.beginDiff();
    }

    @Test
    public void testCommitDiff1() {
	dmap.beginDiff();
	Map<String, DiffKind> diffs = dmap.endDiff();
	assertEquals(0, diffs.size());
    }

    @Test
    public void testCommitDiff2() {
	dmap.put("one", "1");
	dmap.beginDiff();
	Map<String, DiffKind> diffs = dmap.endDiff();
	assertEquals(DiffKind.DELETION, diffs.get("one"));
	assertEquals(0, dmap.size());
    }

    @Test
    public void testCommitDiff3() {
	dmap.put("one", "1");
	dmap.put("two", "2");
	dmap.put("three", "3");
	dmap.put("four", "4");
	dmap.beginDiff();
	dmap.remove("four");
	dmap.put("three", "33");
	dmap.put("two", "22");
	dmap.put("new", "new");
	Map<String, DiffKind> diffs = dmap.endDiff();
	assertEquals(DiffKind.DELETION, diffs.get("four"));
	assertEquals(DiffKind.MODIFICATION, diffs.get("three"));
	assertEquals(DiffKind.MODIFICATION, diffs.get("two"));
	assertEquals(DiffKind.ADDITION, diffs.get("new"));
	assertEquals(DiffKind.DELETION, diffs.get("one"));

	assertEquals(3, dmap.size());
    }

    @Test
    public void testCommitDiff4() {
	dmap.put("one", "1");
	dmap.put("two", "2");
	dmap.put("three", "3");
	dmap.put("four", "4");
	dmap.beginDiff();

	dmap.remove("four");
	dmap.put("four", "44");
	Map<String, DiffKind> diffs = dmap.endDiff();
	assertEquals(DiffKind.MODIFICATION, diffs.get("four"));
	assertEquals(1, dmap.size());
    }

    @Test
    public void testEntrySet1() {
	assertEquals(0, dmap.entrySet().size());
    }

    @Test
    public void testEntrySet2() {
	assertEquals(0, dmap.entrySet().size());
	dmap.put("one", "two");
	dmap.put("one", "two");
	dmap.put("one", "two");
	assertEquals(1, dmap.entrySet().size());
    }

    @Test
    public void testEntrySet3() {
	assertEquals(0, dmap.entrySet().size());
	dmap.put("one", "two");
	dmap.put("three", "two");
	assertEquals(2, dmap.entrySet().size());
    }

    @Test
    public void testEntrySet4() {
	assertEquals(0, dmap.entrySet().size());
	dmap.put("one", "1");
	dmap.put("two", "2");
	dmap.put("three", "3");
	assertEquals(3, dmap.entrySet().size());
	boolean one = false, two = false, three = false;
	for (Entry<String, String> entry : dmap.entrySet()) {
	    if (entry.getKey() == "one") {
		assertFalse(one);
		assertEquals("1", entry.getValue());
		one = true;
	    }
	    if (entry.getKey() == "two") {
		assertFalse(two);
		assertEquals("2", entry.getValue());
		two = true;
	    }
	    if (entry.getKey() == "three") {
		assertFalse(three);
		assertEquals("3", entry.getValue());
		three = true;
	    }
	}
	assertTrue(one);
	assertTrue(two);
	assertTrue(three);
    }

    @Test
    public void testPutKV1() {
	dmap.put("one", "1");
	assertEquals("1", dmap.get("one"));
    }

    @Test
    public void testPutKV2() {
	dmap.put("one", "1");
	assertEquals("1", dmap.get("one"));
	dmap.put("one", "15");
	assertEquals("15", dmap.get("one"));
    }

    @Test
    public void testPutKV3() {
	dmap.beginDiff();
	dmap.put("one", "1");
	assertEquals("1", dmap.get("one"));
	dmap.endDiff();
	assertEquals("1", dmap.get("one"));
    }

    @Test
    public void testPutKV4() {
	dmap.beginDiff();
	dmap.put("one", "1");
	assertEquals("1", dmap.get("one"));
	dmap.put("one", "15");
	assertEquals("15", dmap.get("one"));
	dmap.endDiff();
	assertEquals("15", dmap.get("one"));
    }

    @Test
    public void testRemoveObject1() {
	assertNull(dmap.remove("hello"));
	dmap.put("hello", "world");
	dmap.put("foo", "bar");
	assertEquals("world", dmap.get("hello"));
	assertEquals("bar", dmap.get("foo"));
	assertEquals("bar", dmap.remove("foo"));
	assertNull(dmap.get("foo"));
	assertEquals("world", dmap.remove("hello"));
	assertNull(dmap.get("hello"));
    }

    @Test
    public void testRemoveObject2() {
	assertNull(dmap.remove("hello"));
	dmap.put("hello", "world");
	dmap.put("foo", "bar");
	dmap.beginDiff();
	assertEquals("world", dmap.get("hello"));
	assertEquals("bar", dmap.get("foo"));
	assertEquals("bar", dmap.remove("foo"));
	assertNull(dmap.get("foo"));
	assertEquals("world", dmap.remove("hello"));
	assertNull(dmap.get("hello"));
	dmap.endDiff();
    }

    @Test
    public void testRemoveObject3() {
	assertNull(dmap.remove("hello"));
	dmap.put("hello", "world");
	dmap.put("foo", "bar");
	dmap.beginDiff();
	assertEquals("world", dmap.get("hello"));
	assertEquals("bar", dmap.get("foo"));
	assertEquals("bar", dmap.remove("foo"));
	assertNull(dmap.get("foo"));
	dmap.endDiff();
	assertNull(dmap.get("hello"));
    }

}
