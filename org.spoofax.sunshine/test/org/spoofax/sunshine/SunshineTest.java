package org.spoofax.sunshine;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.spoofax.sunshine.framework.messages.MessageTest;
import org.spoofax.sunshine.framework.messages.PositionRegionTest;
import org.spoofax.sunshine.framework.messages.TokenRegionTest;

@RunWith(Suite.class)
@SuiteClasses({ MessageTest.class, PositionRegionTest.class, TokenRegionTest.class })
public class SunshineTest {

}
