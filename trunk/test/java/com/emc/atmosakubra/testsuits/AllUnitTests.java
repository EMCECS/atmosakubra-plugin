package com.emc.atmosakubra.testsuits;

import com.emc.atmosakubra.TestATMOSBlob;
import com.emc.atmosakubra.utils.TestATMOSConnection;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({TestATMOSBlob.class, TestATMOSConnection.class})
public class AllUnitTests {

}
