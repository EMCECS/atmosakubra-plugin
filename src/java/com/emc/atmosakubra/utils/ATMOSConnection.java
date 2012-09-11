package com.emc.atmosakubra.utils;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.retry.RetryPolicies;
import org.apache.hadoop.io.retry.RetryPolicy;
import org.apache.hadoop.io.retry.RetryProxy;

import com.emc.esu.api.BufferSegment;
import com.emc.esu.api.DirectoryEntry;
import com.emc.esu.api.EsuApi;
import com.emc.esu.api.EsuException;
import com.emc.esu.api.Extent;
import com.emc.esu.api.Identifier;
import com.emc.esu.api.MetadataList;
import com.emc.esu.api.MetadataTag;
import com.emc.esu.api.MetadataTags;
import com.emc.esu.api.ObjectPath;
import com.emc.esu.api.rest.EsuRestApi;

/**
 * Manages connection with ATMOS, provides API for accessing objects on ATMOS
 * 
 */
public class ATMOSConnection {

    protected static final Log LOG = LogFactory.getLog(ATMOSConnection.class);
    private final static Set<Integer> HTTP_RETRY_ERROR_CODES = new HashSet<Integer>(Arrays.asList(0, 500, 409));
    private final int MAX_RETRIES = 3;
    private final int RETRIES_SLEEP_TIME_SECONDS = 1000;
    private String baseDir = "/akubraBase/";
    private EsuApi esuApi;
    private int ioBufferSize = 16536;

    /**
     * Establishes connection with ATMOS, using provided host, port and credentials
     * @param atmosHost
     *            host of machine with ATMOS
     * @param atmosPort
     *            port of machine with ATMOS
     * @param atmosUID
     *            user ID of your account on ATMOS
     * @param atmosSecret
     *            shared secret of your account on ATMOS
     */
    public ATMOSConnection(String atmosHost, int atmosPort, String atmosUID, String atmosSecret) {
	RetryPolicy basePolicy = RetryPolicies.retryUpToMaximumCountWithFixedSleep(MAX_RETRIES,
		RETRIES_SLEEP_TIME_SECONDS, TimeUnit.SECONDS);
	esuApi = applyRetryPolicy(new EsuRestApi(atmosHost, atmosPort), basePolicy);
	esuApi.setUid(atmosUID);
	esuApi.setSecret(atmosSecret);
    }

    /** for unit tests */
    protected ATMOSConnection() {
    }
    
    /** for unit tests */
    protected ATMOSConnection(EsuApi esuAPI) {
	this.esuApi = esuAPI;
    }

    /**
     * Method, that is used by Spring Framework, to set <code>baseDir</code> parameter
     * 
     * @param baseDir
     *            base directory on ATMOS, where digital object XML and
     *            data-streams of FC will be stored
     */
    public void setBaseDir(String baseDir) {
	baseDir = Utils.joinTwoPaths("/", baseDir);
	baseDir = Utils.joinTwoPaths(baseDir, "/");
	this.baseDir = baseDir;
    }

    /**
     * Method, that is used by Spring Framework, to set <code>ioBufferSize</code> parameter
     * 
     * @param ioBufferSize
     *            size of buffer, that will be used internally, during uploading
     *            and downloading of data-streams to ATMOS.
     */
    public void setIoBufferSize(int ioBufferSize) {
	this.ioBufferSize = ioBufferSize;
    }

    /**
     * Convert relative to <code>baseDir</code> path to absolute path  
     * @param path path to object, relative to base directory
     * @return absolute path to object
     */
    public ObjectPath getObjectPath(String path) {
	return new ObjectPath(Utils.joinTwoPaths(baseDir, path));
    }

    /**
     * Open input stream to object on ATMOS
     * @param object absolute path to object
     * @return InputStream instance
     * @throws UnsupportedEncodingException
     */
    public InputStream getObjectInputStream(ObjectPath object) throws UnsupportedEncodingException {
	try{
	LOG.debug("Opening input stream of object " + object);
	return esuApi.readObjectStream(object, null);
	} catch(EsuException e){
	    if(e.getHttpCode() == 404){
		try {
		    Thread.sleep(100);
		} catch (InterruptedException ie) {
		    LOG.debug(ie);
		}
		LOG.debug("Retrying attempt to open input stream of object " + object);
		return esuApi.readObjectStream(object, null);
	    }
	    LOG.error(e);
	    throw e;
	}
    }

    /**
     * List objects on ATMOS within folder
     * @param folder path to folder
     * @return List of relative to folder paths
     */
    public List<String> listAllObjects(String folder) {
	List<String> result = new ArrayList<String>();
	if (folder == null)
	    folder = "";
	ObjectPath rootFolder = new ObjectPath(Utils.joinTwoPaths(baseDir, Utils.joinTwoPaths(folder,"/")));
	List<DirectoryEntry> entries = esuApi.listDirectory(rootFolder);
	for (DirectoryEntry entry : entries) {
	    result.add(entry.getPath().toString().substring(rootFolder.toString().length()));
	}
	return result;
    }

    /**
     * create an empty object on ATMOS
     * @param objectPath path to object
     * @return object ID
     */
    public String createObject(ObjectPath objectPath) {
	LOG.debug("Creating an object " + objectPath);
	ObjectPath path = objectPath;
	Identifier objectId = esuApi.createObjectOnPath(path, null, null, null, null);
	return objectId.toString();
    }

    /**
     * Get size of object in bytes
     * @param objectPath path to object
     * @return size of object in bytes
     */
    public long getObjectSize(ObjectPath objectPath) {
	LOG.debug("Getting size of an object " + objectPath);
	try {
	    MetadataTags tags = new MetadataTags() {
		{
		    addTag(new MetadataTag("size", true));
		}
	    };
	    MetadataList list = esuApi.getSystemMetadata(objectPath, tags);
	    LOG.debug("Size of an object " + objectPath + " is " + list.getMetadata("size").getValue());
	    return Long.parseLong(list.getMetadata("size").getValue());
	} catch (EsuException e) {
	    if (e.getHttpCode() == 404) {
		LOG.debug("Object " + objectPath + " is not found");
		return -1;
	    } else
		throw e;
	}
    }

    /**
     * create byte buffer, that is used by {@link com.emc.atmosakubra.utils.ATMOSOutputStream}
     * @return byte buffer with size <code>ioBufferSize</code>
     */
    public byte[] createIOBuffer() {
	return new byte[ioBufferSize];
    }

    /**
     * Update object on ATMOS
     * @param objectPath path to object
     * @param data byte buffer with data
     * @param dstOffset offset in object to start with
     * @param srcOffset offset in data buffer to start with
     * @param size length of data that should be written from buffer to object
     */
    public void updateObject(ObjectPath objectPath, byte[] data, long dstOffset, int srcOffset, int size) {
	LOG.debug("Updating an object " + objectPath + " New size is " + (dstOffset + size));
	BufferSegment segment = new BufferSegment(data);
	segment.setOffset(srcOffset);
	segment.setSize(size);
	esuApi.updateObjectFromSegment(objectPath, null, null, new Extent(dstOffset, size), segment,
		null);
    }

    /**
     * remove object from ATMOS
     * @param objectPath path to object
     */
    public void delete(ObjectPath objectPath) {
	LOG.debug("Deleting an object " + objectPath);
	esuApi.deleteObject(objectPath);
    }

    /**
     * rename object denoted by <code>srcPath</code>
     * @param srcPath current path of the object
     * @param dstPath The new path for the object
     */
    public void move(ObjectPath srcPath, ObjectPath dstPath) {
	LOG.debug("Moving an object " + srcPath + " to " + dstPath);
	esuApi.rename(srcPath, dstPath, true);
	for(int i = 0; i < 3; i++){
        	try{
        	    esuApi.readObject(dstPath, new Extent(0, 1), null);
        	    break;
        	} catch (EsuException e){
        	    if(e.getHttpCode() == 404 && i < 3){
        		LOG.warn("Waiting for object " + dstPath);
        		try {
			    Thread.sleep(100);
			} catch (InterruptedException ie) {
			    LOG.warn(ie);
			}
        	    } else {
        		throw e;
        	    }
        	}
	}
    }
    
    /**
     * remove object recursively
     * @param esuApi
     * @param path
     */
    public void deleteRecursively(String path) {
	ObjectPath objectPath = new ObjectPath(path);
	try {
	    if (isFolder(objectPath)) {
		List<DirectoryEntry> entries = esuApi.listDirectory(objectPath);
		for (DirectoryEntry entry : entries) {
		    if ("directory".equals(entry.getType())) {
			deleteRecursively(entry.getPath().toString());
		    } else {
			esuApi.deleteObject(entry.getPath());
		    }
		}
	    }
	    esuApi.deleteObject(objectPath);
	} catch (EsuException e) {
	    if (e.getHttpCode() != 404 && e.getAtmosCode() != 1003) {
		throw e;
	    }
	}
    }

    /**
     * Test whether an object in a directory
     * @param path path to object
     * @return true if object is a directory
     */
    public boolean isFolder(ObjectPath object) throws EsuException {
	try {
	    MetadataList metadata = esuApi.getSystemMetadata(object, null);
	    if ("directory".equals(metadata.getMetadata("type").getValue()))
		return true;
	    else
		return false;
	} catch (EsuException e) {
	    if (e.getHttpCode() == 404 || e.getAtmosCode() == 1003) {
		return false;
	    }
	    throw e;
	}
    }
    
    protected EsuApi applyRetryPolicy(EsuApi apiImpl, final RetryPolicy basePolicy) {
	Map<Class<? extends Exception>, RetryPolicy> exceptionToPolicyMap = new HashMap<Class<? extends Exception>, RetryPolicy>();
	exceptionToPolicyMap.put(Exception.class, basePolicy);
	exceptionToPolicyMap.put(EsuException.class, new RetryPolicy() {
	    public boolean shouldRetry(Exception e, int retries) throws Exception {
		final EsuException esuException = (EsuException) e;
		final boolean retry = HTTP_RETRY_ERROR_CODES.contains(esuException.getHttpCode())
			&& basePolicy.shouldRetry(e, retries);
		if (retry) {
		    LOG.warn("RETRYING " + esuException.getHttpCode() + ": " + e.getMessage());
		}

		return retry;
	    }
	});

	RetryPolicy methodPolicy = RetryPolicies.retryByException(RetryPolicies.TRY_ONCE_THEN_FAIL,
		exceptionToPolicyMap);

	Map<String, RetryPolicy> methodNameToPolicyMap = new HashMap<String, RetryPolicy>();

	// ESU API methods
	methodNameToPolicyMap.put("createObject", methodPolicy);
	methodNameToPolicyMap.put("createObjectOnPath", methodPolicy);
	methodNameToPolicyMap.put("createObjectFromSegment", methodPolicy);
	methodNameToPolicyMap.put("createObjectFromSegmentOnPath", methodPolicy);
	methodNameToPolicyMap.put("deleteObject", methodPolicy);
	methodNameToPolicyMap.put("deleteUserMetadata", methodPolicy);
	methodNameToPolicyMap.put("setAcl", methodPolicy);
	methodNameToPolicyMap.put("getAcl", methodPolicy);
	methodNameToPolicyMap.put("getAllMetadata", methodPolicy);
	methodNameToPolicyMap.put("getListableTags", methodPolicy);
	methodNameToPolicyMap.put("getSystemMetadata", methodPolicy);
	methodNameToPolicyMap.put("getUserMetadata", methodPolicy);
	methodNameToPolicyMap.put("listDirectory", methodPolicy);
	methodNameToPolicyMap.put("listObjects", methodPolicy);
	methodNameToPolicyMap.put("listObjectsWithMetadata", methodPolicy);
	methodNameToPolicyMap.put("listUserMetadataTags", methodPolicy);
	methodNameToPolicyMap.put("readObject", methodPolicy);
	methodNameToPolicyMap.put("readObjectStream", methodPolicy);
	methodNameToPolicyMap.put("updateObject", methodPolicy);
	methodNameToPolicyMap.put("updateObjectFromSegment", methodPolicy);
	methodNameToPolicyMap.put("updateObjectFromStream", methodPolicy);
	methodNameToPolicyMap.put("getShareableUrl", methodPolicy);

	return (EsuApi) RetryProxy.create(EsuApi.class, apiImpl, methodNameToPolicyMap);
    }

}
