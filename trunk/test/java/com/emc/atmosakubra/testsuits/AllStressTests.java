package com.emc.atmosakubra.testsuits;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.emc.atmosakubra.stress.StressTestAtmosAkubraPlugin;

@RunWith(Suite.class)
@Suite.SuiteClasses({StressTestAtmosAkubraPlugin.class})
public class AllStressTests {

}
