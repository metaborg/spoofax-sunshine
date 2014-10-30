package org.spoofax.sunshine.services.files;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.metaborg.sunshine.pipeline.diff.DiffKind;
import org.metaborg.sunshine.pipeline.diff.MultiDiff;
import org.metaborg.sunshine.services.filesource.DirMonitor;
import org.metaborg.util.resource.ExtensionFileSelector;

public class DirMonitorTest {
	private Set<String> extensions;
	private FileSelector selector;
	private FileSystemManager fsm;
	private FileObject cacheDir, dir;
	private DirMonitor mon;

	@Before
	public void setUp() throws Exception {
		extensions = new HashSet<String>();
		extensions.add("foo");
		extensions.add("bar");

		selector = new ExtensionFileSelector(extensions);

		fsm = VFS.getManager();

		dir = fsm.resolveFile("ram:///test");
		assertFalse(dir.exists());
		dir.createFolder();

		cacheDir = dir.resolveFile("cache");
		assertFalse(cacheDir.exists());
		cacheDir.createFolder();
		assertTrue(cacheDir.exists());
	}

	@After
	public void tearDown() throws Exception {
		assertTrue(dir.exists());
		dir.delete(new AllFileSelector());
		assertFalse(dir.exists());
		mon = null;
		extensions = null;
	}

	@Test
	public void testDirMonitor1() {
		mon = new DirMonitor(dir, cacheDir, selector, fsm);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDirMonitor2() {
		mon = new DirMonitor(null, cacheDir, selector, fsm);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDirMonitor3() {
		mon = new DirMonitor(dir, null, selector, fsm);
	}

	@Test
	public void testGetChanges1() throws IOException {
		mon = new DirMonitor(dir, cacheDir, selector, fsm);
		assertEquals(0, mon.getChanges().size());
		assertEquals(0, mon.getChanges().size());
	}

	@Test
	public void testGetChanges2() throws IOException {
		dir.resolveFile("haihui.foo").createFile();
		mon = new DirMonitor(dir, cacheDir, selector, fsm);
		assertEquals(1, mon.getChanges().size());
		assertEquals(0, mon.getChanges().size());
	}

	@Test
	public void testGetChanges3() throws IOException {
		dir.resolveFile("foo.foo").createFile();
		mon = new DirMonitor(dir, cacheDir, selector, fsm);
		assertEquals(1, mon.getChanges().size());
		dir.resolveFile("foo.foo").moveTo(dir.resolveFile("foo.bar"));
		assertTrue(dir.resolveFile("foo.bar").exists());
		assertFalse(dir.resolveFile("foo.foo").exists());
		assertEquals(2, mon.getChanges().size());
		assertEquals(0, mon.getChanges().size());
	}

	@Test
	public void testGetChanges4() throws IOException {
		dir.resolveFile("foo.foo").createFile();
		dir.resolveFile("foo.bar").createFile();
		dir.resolveFile("foo2.bar").createFile();
		mon = new DirMonitor(dir, cacheDir, selector, fsm);
		assertEquals(3, mon.getChanges().size());
		assertEquals(0, mon.getChanges().size());
	}

	@Test
	public void testGetChanges5() throws IOException {
		mon = new DirMonitor(dir, cacheDir, selector, fsm);
		mon.getChanges();
		FileObject dirdir = dir.resolveFile("foobar");
		dirdir.createFolder();
		assertEquals(0, mon.getChanges().size());
		dirdir.resolveFile("bull.foo").createFile();
		assertEquals(1, mon.getChanges().size());
		assertEquals(0, mon.getChanges().size());
	}

	@Test
	public void testGetChanges6() throws IOException, InterruptedException {
		dir.resolveFile("lol.foo").createFile();
		mon = new DirMonitor(dir, cacheDir, selector, fsm);
		assertEquals(1, mon.getChanges().size());
		dir.resolveFile("lols.foo").createFile();
		assertEquals(1, mon.getChanges().size());
		// addition
		dir.resolveFile("newfile.bar").createFile();
		// modification
		Thread.sleep(1000);
		OutputStream os = dir.resolveFile("lols.foo").getContent()
				.getOutputStream();
		os.write(42);
		os.write(55);
		os.write(128);
		os.write(5);
		os.flush();
		os.close();
		// deletion & addition
		dir.resolveFile("lol.foo").moveTo(dir.resolveFile("movedhere.foo"));
		MultiDiff<FileObject> changes = mon.getChanges();
		assertEquals(2, changes.getDiff(DiffKind.ADDITION).size());
		assertEquals(1, changes.getDiff(DiffKind.MODIFICATION).size());
		assertEquals(1, changes.getDiff(DiffKind.DELETION).size());
		assertEquals(0, mon.getChanges().size());
	}

	@Test
	public void testGetChanges7() throws IOException, InterruptedException {
		dir.resolveFile("lol.foo").createFile();
		mon = new DirMonitor(dir, cacheDir, selector, fsm);
		assertEquals(1, mon.getChanges().size());
		assertEquals(0, mon.getChanges().size());
		assertEquals(0, mon.getChanges().size());
		Thread.sleep(1000);
		OutputStream os = dir.resolveFile("lol.foo").getContent()
				.getOutputStream();
		os.write(42);
		os.flush();
		os.close();
		MultiDiff<FileObject> diff = mon.getChanges();
		assertEquals(1, diff.size());
		assertEquals(1, diff.getDiff(DiffKind.MODIFICATION).size());
	}

	@Test
	public void testGetChanges8() throws IOException, InterruptedException {
		final int NUMFILES = 10000;
		mon = new DirMonitor(dir, cacheDir, selector, fsm);
		assertEquals(0, mon.getChanges().size());
		Collection<FileObject> files = new LinkedList<FileObject>();
		for (int idx = 1; idx <= NUMFILES; idx++) {
			FileObject f = dir.resolveFile(UUID.randomUUID().toString()
					+ ".foo");
			files.add(f);
			f.createFile();
		}
		assertEquals(NUMFILES, mon.getChanges().getDiff(DiffKind.ADDITION)
				.size());
		int idx = 1;
		for (FileObject file : files) {
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
		mon = new DirMonitor(dir, cacheDir, selector, fsm);
		assertEquals(0, mon.getChanges().size());
		Collection<FileObject> files = new LinkedList<FileObject>();
		for (int idx = 1; idx <= NUMFILES; idx++) {
			FileObject f = dir.resolveFile(UUID.randomUUID().toString()
					+ ".foo");
			files.add(f);
			f.createFile();
		}
		assertEquals(NUMFILES, mon.getChanges().getDiff(DiffKind.ADDITION)
				.size());
		mon = null;
		int idx = 1;
		for (FileObject file : files) {
			if (idx > NUMFILES / 2)
				break;
			assertTrue(file.delete());
			idx++;
		}
		mon = new DirMonitor(dir, cacheDir, selector, fsm);
		assertEquals(NUMFILES / 2, mon.getChanges().getDiff(DiffKind.DELETION)
				.size());
	}

}
