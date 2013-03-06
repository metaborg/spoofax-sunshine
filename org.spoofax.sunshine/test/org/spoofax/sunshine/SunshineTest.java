package org.spoofax.sunshine;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.spoofax.sunshine.framework.language.ALanguageTest;
import org.spoofax.sunshine.framework.language.AdHocJarBasedLanguageTest;
import org.spoofax.sunshine.framework.messages.MessageTest;
import org.spoofax.sunshine.framework.messages.PositionRegionTest;
import org.spoofax.sunshine.framework.messages.TokenRegionTest;
import org.spoofax.sunshine.parser.framework.AParserTest;
import org.spoofax.sunshine.parser.framework.FileBasedParseTableProviderTest;

@RunWith(Suite.class)
@SuiteClasses({ MessageTest.class, PositionRegionTest.class, TokenRegionTest.class, ALanguageTest.class,
		AdHocJarBasedLanguageTest.class, FileBasedParseTableProviderTest.class, AParserTest.class })
public class SunshineTest {

}
