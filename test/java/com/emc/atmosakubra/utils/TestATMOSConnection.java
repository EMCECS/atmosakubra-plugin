package com.emc.atmosakubra.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.apache.hadoop.io.retry.RetryPolicies;
import org.apache.hadoop.io.retry.RetryPolicy;
import org.junit.Test;

import com.emc.atmosakubra.utils.ATMOSConnection;
import com.emc.esu.api.Acl;
import com.emc.esu.api.DirectoryEntry;
import com.emc.esu.api.EsuApi;
import com.emc.esu.api.EsuException;
import com.emc.esu.api.Identifier;
import com.emc.esu.api.Metadata;
import com.emc.esu.api.MetadataList;
import com.emc.esu.api.MetadataTags;
import com.emc.esu.api.ObjectId;
import com.emc.esu.api.ObjectPath;
import com.emc.esu.api.rest.MockEsuApi;

public class TestATMOSConnection {

    @Test
    public void testListAllObjects() {
	EsuApi mockEsuApi = new MockEsuApi() {

	    @Override
	    public List<DirectoryEntry> listDirectory(ObjectPath path) {
		if (path.toString().startsWith("/base/A/")) {
		    List<DirectoryEntry> dirEntries = new ArrayList<DirectoryEntry>();
		    DirectoryEntry entry = new DirectoryEntry();
		    entry.setPath(new ObjectPath("/base/A/1.file"));
		    dirEntries.add(entry);

		    DirectoryEntry entry2 = new DirectoryEntry();
		    entry2.setPath(new ObjectPath("/base/A/2.file"));
		    dirEntries.add(entry2);
		    return dirEntries;
		} else if (path.toString().startsWith("/base")) {
		    List<DirectoryEntry> dirEntries = new ArrayList<DirectoryEntry>();
		    DirectoryEntry entry = new DirectoryEntry();
		    entry.setPath(new ObjectPath("/base/A/"));
		    dirEntries.add(entry);
		    return dirEntries;
		} else {
		    throw new EsuException("requested object was not found.", 404, 1003);
		}

	    }
	};
	ATMOSConnection connection = new ATMOSConnection(mockEsuApi);
	connection.setBaseDir("/base/");
	List<String> result = connection.listAllObjects("A");
	Assert.assertEquals(2, result.size());
	Assert.assertEquals("1.file", result.get(0));
	Assert.assertEquals("2.file", result.get(1));
	result = connection.listAllObjects(null);
	Assert.assertEquals(1, result.size());
	Assert.assertEquals("A/", result.get(0));
    }
    
    @Test
    public void testDeleteRecursively(){
	final Set<String> removedObjects = new HashSet<String>();
	EsuApi mockEsuApi = new MockEsuApi() {

	    @Override
	    public List<DirectoryEntry> listDirectory(ObjectPath path) {
		if(path.toString().startsWith("/base/A/B/")){
		    List<DirectoryEntry> dirEntries = new ArrayList<DirectoryEntry>();
		    DirectoryEntry entry = new DirectoryEntry();
		    entry.setType("regular");
		    entry.setPath(new ObjectPath("/base/A/B/3.file"));
		    dirEntries.add(entry);
		    return dirEntries;
		} else if (path.toString().startsWith("/base/A/")) {
		    List<DirectoryEntry> dirEntries = new ArrayList<DirectoryEntry>();
		    DirectoryEntry entry = new DirectoryEntry();
		    entry.setType("regular");
		    entry.setPath(new ObjectPath("/base/A/1.file"));
		    dirEntries.add(entry);
		    DirectoryEntry entry2 = new DirectoryEntry();
		    entry2.setPath(new ObjectPath("/base/A/2.file"));
		    entry2.setType("regular");
		    dirEntries.add(entry2);
		    DirectoryEntry entry3 = new DirectoryEntry();
		    entry3.setPath(new ObjectPath("/base/A/B/"));
		    entry3.setType("directory");
		    dirEntries.add(entry3);
		    return dirEntries;
		} else if (path.toString().startsWith("/base")) {
		    List<DirectoryEntry> dirEntries = new ArrayList<DirectoryEntry>();
		    DirectoryEntry entry = new DirectoryEntry();
		    entry.setPath(new ObjectPath("/base/A/"));
		    entry.setType("directory");
		    dirEntries.add(entry);
		    return dirEntries;
		} else {
		    throw new EsuException("requested object was not found.", 404, 1003);
		}
	    }
	    @Override
	    public void deleteObject(Identifier id){
		removedObjects.add(id.toString());
	    }
	    public MetadataList getSystemMetadata(Identifier id, MetadataTags tags) {
		MetadataList list = new MetadataList(){
		    {
			addMetadata(new Metadata("type", "directory", true));
		    }
		};
	        return list;
	    }
	};
	ATMOSConnection connection = new ATMOSConnection(mockEsuApi);
	connection.setBaseDir("/base/");
	connection.deleteRecursively("/base/A/");
	Assert.assertEquals(5, removedObjects.size());
	Assert.assertTrue(removedObjects.contains("/base/A/"));
	Assert.assertTrue(removedObjects.contains("/base/A/1.file"));
	Assert.assertTrue(removedObjects.contains("/base/A/2.file"));
	Assert.assertTrue(removedObjects.contains("/base/A/B/"));
	Assert.assertTrue(removedObjects.contains("/base/A/B/3.file"));
    }
    
    @Test
    public void testApplyRetryPolicy(){
	final Map<String, Integer> numberOfTimesMethodCalled = new HashMap<String, Integer>();
	EsuApi mockEsuApi = new MockEsuApi() {
	    @Override
	    public ObjectId createObject(Acl acl, MetadataList metadata, byte[] data, String mimeType) {
	      increaseNumberOfTimesMethodCalled("createObject");
	      throw new EsuException("", 500, 1003);
	    }

	    @Override
	    public void setAcl(Identifier id, Acl acl) {
	      increaseNumberOfTimesMethodCalled("setAcl");
	      throw new EsuException("", 409, 1003);
	    }

	    @Override
	    public void deleteObject(Identifier id) {
	      increaseNumberOfTimesMethodCalled("deleteObject");
	      throw new EsuException("", 0, 1003);
	    }

	    @Override
	    public Acl getAcl(Identifier id) {
	      increaseNumberOfTimesMethodCalled("getAcl");
	      throw new EsuException("", 403, 1003);
	    }
	      
	    private void increaseNumberOfTimesMethodCalled(String methodName) {
	      if (!numberOfTimesMethodCalled.containsKey(methodName))
	        numberOfTimesMethodCalled.put(methodName, 0);
	      numberOfTimesMethodCalled.put(methodName, numberOfTimesMethodCalled.get(methodName) + 1);
	    }
	};
	ATMOSConnection connection = new ATMOSConnection();
	RetryPolicy basePolicy = RetryPolicies.retryUpToMaximumCountWithFixedSleep(3,
		1, TimeUnit.SECONDS);
	mockEsuApi = connection.applyRetryPolicy(mockEsuApi, basePolicy);
	try {
	    mockEsuApi.createObject(null, null, null, null);
	      Assert.fail();
	    } catch (EsuException e) {
	      Assert.assertEquals(4, numberOfTimesMethodCalled.get("createObject").intValue());
	    }
	    try {
		mockEsuApi.setAcl(null, null);
	      Assert.fail();
	    } catch (EsuException e) {
	      Assert.assertEquals(4, numberOfTimesMethodCalled.get("setAcl").intValue());
	    }
	    try {
		mockEsuApi.deleteObject(null);
	      Assert.fail();
	    } catch (EsuException e) {
	      Assert.assertEquals(4, numberOfTimesMethodCalled.get("deleteObject").intValue());
	    }
	    try {
		mockEsuApi.getAcl(null);
	      Assert.fail();
	    } catch (EsuException e) {
	      Assert.assertEquals(1, numberOfTimesMethodCalled.get("getAcl").intValue());
	    }

    }

}
