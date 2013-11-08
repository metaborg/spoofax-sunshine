package org.spoofax.sunshine;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.spoofax.sunshine.services.files.DirMonitorTest;
import org.spoofax.sunshine.util.BubblingMapTest;
import org.spoofax.sunshine.util.DiffingHashMapTest;

@RunWith(Suite.class)
@SuiteClasses({ BubblingMapTest.class,
		DiffingHashMapTest.class, DirMonitorTest.class })
public class SunshineTest {

}
