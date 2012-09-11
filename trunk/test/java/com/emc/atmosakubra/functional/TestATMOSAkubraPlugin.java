package com.emc.atmosakubra.functional;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import junit.framework.Assert;

import org.fcrepo.apia.ArrayOfString;
import org.fcrepo.apia.FedoraAPIA;
import org.fcrepo.apia.FieldSearchQuery;
import org.fcrepo.apia.FieldSearchResult;
import org.fcrepo.apia.FieldSearchResult.ResultList;
import org.fcrepo.apia.MIMETypedStream;
import org.fcrepo.apia.ObjectFields;
import org.fcrepo.apim.Datastream;
import org.fcrepo.apim.FedoraAPIM;
import org.junit.Test;

public class TestATMOSAkubraPlugin extends BaseFunctionalTest {

    public TestATMOSAkubraPlugin() throws Exception {
	super();
    }

    @Test
    public void testCreateObject() throws RemoteException, FileNotFoundException, IOException {
	FedoraAPIM apiM = createAPIMInstance();
	FedoraAPIA apiA = createAPIAInstance();
	try {
	    File objectFile = getResource("testCreateObject.xml", TestATMOSAkubraPlugin.class);
	    File expectedFile = getResource("test-data/PIC_0029.JPG", TestATMOSAkubraPlugin.class);
	    String pid = ingest(apiM, new FileInputStream(objectFile), "info:fedora/fedora-system:FOXML-1.1",
		    "testCreateObject");
	    Assert.assertNotNull(pid);
	    Datastream ds = apiM.getDatastream(pid, "IMG", null);
	    Assert.assertEquals("Size of object's stream in FC", expectedFile.length(), ds.getSize());
	    MIMETypedStream stream = apiA.getDatastreamDissemination(pid, "IMG", null);
	    File actualFile = createFileFromBuffer(stream.getStream());
	    Assert.assertTrue("Files " + expectedFile.getCanonicalPath() + " and " + actualFile.getCanonicalPath() + " are not equal", compare(actualFile, expectedFile));
	}finally {
	    purgeObject(apiM, "testCreateObject:1", "testing purge", true);
	}
    }
    
    @Test
    public void testListObjects() throws IOException{
	FedoraAPIM apiM = createAPIMInstance();
	FedoraAPIA apiA = createAPIAInstance();
	Map<String, File> objectFiles = new LinkedHashMap<String, File>();
	objectFiles.put("testListObjects:PIC_0029", getResource("testListObjects_PIC_0029.xml", TestATMOSAkubraPlugin.class));
	objectFiles.put("testListObjects:PIC_0030", getResource("testListObjects_PIC_0030.xml", TestATMOSAkubraPlugin.class));
	objectFiles.put("testListObjects:PIC_0031", getResource("testListObjects_PIC_0031.xml", TestATMOSAkubraPlugin.class));
	objectFiles.put("testListObjects:PIC_0032", getResource("testListObjects_PIC_0032.xml", TestATMOSAkubraPlugin.class));
	objectFiles.put("testListObjects:AllObjects", getResource("testListObjects_AllObjects.xml", TestATMOSAkubraPlugin.class));
	try{
	    for(Map.Entry<String, File> entry : objectFiles.entrySet()){
		ingest(apiM, new FileInputStream(entry.getValue()), "info:fedora/fedora-system:FOXML-1.1", entry.getKey());
	    }
	    FieldSearchQuery query = new FieldSearchQuery();
	    query.setTerms(new JAXBElement<String>(new QName("terms"), String.class, "testListObjects*"));
	    FieldSearchResult result = apiA.findObjects(new ArrayOfString(new String[]{"pid"}), new BigInteger("10"), query);
	    ResultList resultList = result.getResultList();
	    List<ObjectFields> fields = resultList.getObjectFields();
	    Assert.assertEquals(5, fields.size());
	} finally {
	    for(Map.Entry<String, File> entry : objectFiles.entrySet()){
		purgeObject(apiM, entry.getKey(), "testing purge", true);
	    }
	}
    }
    
    @Test
    public void testExistMethod() throws IOException{
	FedoraAPIM apiM = createAPIMInstance();
	FedoraAPIA apiA = createAPIAInstance();
	try {
	    File objectFile = getResource("testCreateObject.xml", TestATMOSAkubraPlugin.class);
	    String pid = ingest(apiM, new FileInputStream(objectFile), "info:fedora/fedora-system:FOXML-1.1",
		    "testCreateObject");
	    Assert.assertNotNull(pid);
	    FieldSearchQuery query = new FieldSearchQuery();
	    query.setTerms(new JAXBElement<String>(new QName("terms"), String.class, "testCreateObject:1"));
	    FieldSearchResult result = apiA.findObjects(new ArrayOfString(new String[]{"pid"}), new BigInteger("10"), query);
	    ResultList resultList = result.getResultList();
	    List<ObjectFields> fields = resultList.getObjectFields();
	    Assert.assertEquals(1, fields.size());
	}finally {
	    purgeObject(apiM, "testCreateObject:1", "testing purge", true);
	}
    }

}
