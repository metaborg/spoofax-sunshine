package org.spoofax.sunshine.services.files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spoofax.sunshine.pipeline.diff.DiffKind;
import org.spoofax.sunshine.pipeline.diff.MultiDiff;
import org.spoofax.sunshine.services.filesource.DirMonitor;

public class DirMonitorTest {

    Set<String> extensions;

    File cacheDir, dir;
    DirMonitor mon;

    @Before
    public void setUp() throws Exception {
	dir = new File("testTemp");
	assertFalse(dir.exists());
	dir.mkdirs();
	cacheDir = new File(dir, "testCache");
	assertFalse(cacheDir.exists());
	cacheDir.mkdirs();
	assertTrue(cacheDir.exists());
	extensions = new HashSet<String>();
	extensions.add("foo");
	extensions.add("bar");
    }

    @After
    public void tearDown() throws Exception {
	assertTrue(dir.exists());
	FileUtils.deleteDirectory(dir);
	assertFalse(dir.exists());
	mon = null;
	extensions = null;
    }

    @Test
    public void testDirMonitor1() {
	mon = new DirMonitor(extensions, dir, cacheDir);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDirMonitor2() {
	mon = new DirMonitor(extensions, null, cacheDir);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDirMonitor3() {
	mon = new DirMonitor(extensions, dir, null);
    }

    @Test
    public void testGetChanges1() {
	mon = new DirMonitor(extensions, dir, cacheDir);
	assertEquals(0, mon.getChanges().size());
	assertEquals(0, mon.getChanges().size());
    }

    @Test
    public void testGetChanges2() throws IOException {
	assertTrue(new File(dir, "haihui.foo").createNewFile());
	mon = new DirMonitor(extensions, dir, cacheDir);
	assertEquals(1, mon.getChanges().size());
	assertEquals(0, mon.getChanges().size());
    }

    @Test
    public void testGetChanges3() throws IOException {
	new File(dir, "foo.foo").createNewFile();
	mon = new DirMonitor(extensions, dir, cacheDir);
	assertEquals(1, mon.getChanges().size());
	new File(dir, "foo.foo").renameTo(new File(dir, "foo.bar"));
	assertTrue(new File(dir, "foo.bar").exists());
	assertFalse(new File(dir, "foo.foo").exists());
	assertEquals(2, mon.getChanges().size());
	assertEquals(0, mon.getChanges().size());
    }

    @Test
    public void testGetChanges4() throws IOException {
	assertTrue(new File(dir, "foo.foo").createNewFile());
	assertTrue(new File(dir, "foo.bar").createNewFile());
	assertTrue(new File(dir, "foo2.bar").createNewFile());
	mon = new DirMonitor(extensions, dir, cacheDir);
	assertEquals(3, mon.getChanges().size());
	assertEquals(0, mon.getChanges().size());
    }

    @Test
    public void testGetChanges5() throws IOException {
	mon = new DirMonitor(extensions, dir, cacheDir);
	mon.getChanges();
	File dirdir = new File(dir, "foobar");
	dirdir.mkdir();
	assertEquals(0, mon.getChanges().size());
	new File(dirdir, "bull.foo").createNewFile();
	assertEquals(1, mon.getChanges().size());
	assertEquals(0, mon.getChanges().size());
    }

    @Test
    public void testGetChanges6() throws IOException, InterruptedException {
	assertTrue(new File(dir, "lol.foo").createNewFile());
	mon = new DirMonitor(extensions, dir, cacheDir);
	assertEquals(1, mon.getChanges().size());
	assertTrue(new File(dir, "lols.foo").createNewFile());
	assertEquals(1, mon.getChanges().size());
	// addition
	assertTrue(new File(dir, "newfile.bar").createNewFile());
	// modification
	Thread.sleep(1000);
	FileOutputStream os = new FileOutputStream(new File(dir, "lols.foo"));
	os.write(42);
	os.write(55);
	os.write(128);
	os.write(5);
	os.flush();
	os.close();
	// deletion & addition
	assertTrue(new File(dir, "lol.foo").renameTo(new File(dir,
		"movedhere.foo")));
	MultiDiff<File> changes = mon.getChanges();
	assertEquals(2, changes.getDiff(DiffKind.ADDITION).size());
	assertEquals(1, changes.getDiff(DiffKind.MODIFICATION).size());
	assertEquals(1, changes.getDiff(DiffKind.DELETION).size());
	assertEquals(0, mon.getChanges().size());
    }

    @Test
    public void testGetChanges7() throws IOException, InterruptedException {
	assertTrue(new File(dir, "lol.foo").createNewFile());
	mon = new DirMonitor(extensions, dir, cacheDir);
	assertEquals(1, mon.getChanges().size());
	assertEquals(0, mon.getChanges().size());
	assertEquals(0, mon.getChanges().size());
	Thread.sleep(1000);
	FileOutputStream os = new FileOutputStream(new File(dir, "lol.foo"));
	os.write(42);
	os.flush();
	os.close();
	MultiDiff<File> diff = mon.getChanges();
	assertEquals(1, diff.size());
	assertEquals(1, diff.getDiff(DiffKind.MODIFICATION).size());
    }

    @Test
    public void testGetChanges8() throws IOException, InterruptedException {
	final int NUMFILES = 10000;
	mon = new DirMonitor(extensions, dir, cacheDir);
	assertEquals(0, mon.getChanges().size());
	Collection<File> files = new LinkedList<File>();
	for (int idx = 1; idx <= NUMFILES; idx++) {
	    File f = new File(dir, UUID.randomUUID().toString() + ".foo");
	    files.add(f);
	    assertTrue(f.createNewFile());
	}
	assertEquals(NUMFILES / 2, mon.getChanges().getDiff(DiffKind.ADDITION)
		.size());
	int idx = 1;
	for (File file : files) {
	    if (idx > NUMFILES / 2)
		break;
	    assertTrue(file.delete());
	    idx++;
	}
	assertEquals(NUMFILES / 2, mon.getChanges().getDiff(DiffKind.DELETION)
		.size());
    }

    @Test
    public void testPersist1() throws IOException {
	final int NUMFILES = 10000;
	mon = new DirMonitor(extensions, dir, cacheDir);
	assertEquals(0, mon.getChanges().size());
	Collection<File> files = new LinkedList<File>();
	for (int idx = 1; idx <= NUMFILES; idx++) {
	    File f = new File(dir, UUID.randomUUID().toString() + ".foo");
	    files.add(f);
	    assertTrue(f.createNewFile());
	}
	assertEquals(NUMFILES, mon.getChanges().getDiff(DiffKind.ADDITION)
		.size());
	mon = null;
	int idx = 1;
	for (File file : files) {
	    if (idx > NUMFILES / 2)
		break;
	    assertTrue(file.delete());
	    idx++;
	}
	mon = new DirMonitor(extensions, dir, cacheDir);
	assertEquals(NUMFILES / 2, mon.getChanges().getDiff(DiffKind.DELETION)
		.size());
    }

}
