package com.emc.atmosakubra.stress;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import junit.framework.Assert;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.fcrepo.apia.FedoraAPIA;
import org.fcrepo.apia.MIMETypedStream;
import org.fcrepo.apim.Datastream;
import org.fcrepo.apim.FedoraAPIM;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class StressTestAtmosAkubraPlugin extends BaseStressTest {

    private static final Logger LOG = Logger.getLogger(StressTestAtmosAkubraPlugin.class);
    private FedoraAPIM apiM;
    private FedoraAPIA apiA;
    List<String> objectsList = new LinkedList<String>();
    File fileWithContent;
    List<Long> timeSpentForDeletion = Collections.synchronizedList(new ArrayList<Long>());
    List<Long> timeSpentForRetrieval = Collections.synchronizedList(new ArrayList<Long>());
    List<Long> timeSpentForIngestion = Collections.synchronizedList(new ArrayList<Long>());
    AtomicLong numberOfComparisonErrors = new AtomicLong(0);

    static {
	LOG.setLevel(Level.ALL);
	LOG.addAppender(new ConsoleAppender(new SimpleLayout(), "System.out"));
    }

    public StressTestAtmosAkubraPlugin() throws MalformedURLException {
	super();
    }

    @Before
    public void init() throws IOException {
	apiM = createAPIMInstance();
	apiA = createAPIAInstance();
	fileWithContent = createFileWithRandomContent(OBJECT_DATASTREAM_SIZE);
    }

    @After
    public void cleanUp() {
	apiM = null;
    }

    @Test
    public void testCreateAndPurgeNObjectInMThreads() throws IOException, InterruptedException {
	ExecutorService service = Executors.newFixedThreadPool(THREADS_NUMBER);
	final int objectPerThread = Math.max(1, OBJECTS_NUMBER / THREADS_NUMBER);
	
	for (int i = 0; i < THREADS_NUMBER; i++) {
	    service.execute(new Runnable() {
		public void run() {
		    try {
			ingestNObjects(objectPerThread, fileWithContent);
		    } catch (IOException e) {
			LOG.error(e);
			Assert.fail();
		    }
		}
	    });
	}
	service.shutdown();
	service.awaitTermination(24, TimeUnit.HOURS);
	service = Executors.newFixedThreadPool(THREADS_NUMBER);
	LOG.info("purging " + objectsList.size() + " objects in " + THREADS_NUMBER  + " threads");
	for (int i = 0; i < THREADS_NUMBER; i++) {
	    service.execute(new Runnable() {
		public void run() {
		    try {
			purgeObjects();
		    } catch (IOException e) {
			LOG.error(e);
			Assert.fail();
		    }
		}
	    });
	}
	service.shutdown();
	service.awaitTermination(24, TimeUnit.HOURS);
	outputStatistics();
    }

    public void outputStatistics() {
	if(numberOfComparisonErrors.get() > 0){
	    LOG.error("10 comparison errors");
	}
	//Ingestion
	LOG.info(String.format("%d objects ingested from %d threads in %d ms", OBJECTS_NUMBER, THREADS_NUMBER,
		calculateTotal(timeSpentForIngestion)));
	LOG.info(String.format("Ingestion %.2f Kb/sec",
		calculateSpeed(OBJECTS_NUMBER * OBJECT_DATASTREAM_SIZE, calculateTotal(timeSpentForIngestion))));
	LOG.info(String.format("Average ingestion time %d ms", calculateMean(timeSpentForIngestion)));
	LOG.info(String.format("Ingestion time stdev %.2f ms", calculateStandardDeviation(timeSpentForIngestion)));
	//Retrieval
	LOG.info(String.format("%d objects retrieved from %d threads in %d ms", OBJECTS_NUMBER, THREADS_NUMBER,
		calculateTotal(timeSpentForRetrieval)));
	LOG.info(String.format("Retreaval %.2f Kb/sec",
		calculateSpeed(OBJECTS_NUMBER * OBJECT_DATASTREAM_SIZE, calculateTotal(timeSpentForRetrieval))));
	LOG.info(String.format("Average retrieval time %d ms", calculateMean(timeSpentForRetrieval)));
	LOG.info(String.format("Retrieval time stdev %.2f ms", calculateStandardDeviation(timeSpentForRetrieval)));
	//Deletion
	LOG.info(String.format("%d objects purged from %d threads in %d ms", OBJECTS_NUMBER, THREADS_NUMBER,
		calculateTotal(timeSpentForDeletion)));
	LOG.info(String.format("Average purge time %d ms", calculateMean(timeSpentForDeletion)));
	LOG.info(String.format("Purge time stdev %.2f ms", calculateStandardDeviation(timeSpentForDeletion)));
    }

    public void ingestNObjects(int numberOfObjects, File fileWithContent) throws IOException {
	LOG.info("putting " + numberOfObjects + " objects of size " + OBJECT_DATASTREAM_SIZE + " in " + Thread.currentThread().getName()  + " thread");
	Random random = new Random();
	for (int i = 0; i < numberOfObjects; i++) {
	    File objectFile = createFCObject("testCreateNObjectInNThreads:" + Math.abs(random.nextInt()),
		    fileWithContent.getAbsolutePath());
	    long startTime = System.currentTimeMillis();
	    String pid = ingest(apiM, new FileInputStream(objectFile), "info:fedora/fedora-system:FOXML-1.1",
		    "testCreateObject");
	    timeSpentForIngestion.add(System.currentTimeMillis() - startTime);
	    Assert.assertNotNull(pid);
	    pushPID(pid);
	}
    }

    public void purgeObjects() throws RemoteException {
	String pid = pullPID();
	while (pid != null) {
	    try {
		long startTime = System.currentTimeMillis();
		Datastream ds = apiM.getDatastream(pid, "IMG", null);
		MIMETypedStream stream = apiA.getDatastreamDissemination(pid, "IMG", null);
		Assert.assertEquals("Size of object's stream in FC", fileWithContent.length(), ds.getSize());
		byte[] inBuffer = stream.getStream();
		timeSpentForRetrieval.add(System.currentTimeMillis() - startTime);
		File actualFile = createFileFromBuffer(inBuffer);
		inBuffer = null;
		System.gc();
		Assert.assertTrue(
			"Files " + fileWithContent.getCanonicalPath() + " and " + actualFile.getCanonicalPath()
				+ " are not equal", compare(actualFile, fileWithContent));
	    } catch (IOException e) {
		LOG.error("Comparison failure.");
		numberOfComparisonErrors.addAndGet(1);
	    } finally {
		long startTime = System.currentTimeMillis();
		purgeObject(apiM, pid, "testing purge", true);
		timeSpentForDeletion.add(System.currentTimeMillis() - startTime);
		pid = pullPID();
	    }

	}
    }

    public synchronized void pushPID(String pid) {
	objectsList.add(pid);
    }

    public synchronized String pullPID() {
	if (objectsList.isEmpty())
	    return null;
	String pid = objectsList.get(0);
	objectsList.remove(0);
	return pid;
    }

    private File createFCObject(String pid, String contentPath) throws IOException {
	File template = getResource("testCreateObject.xml", StressTestAtmosAkubraPlugin.class);
	File objectFile = new File(temporaryFolder, UUID.randomUUID().toString());
	copyfile(template, objectFile);
	replaceStringInFile(objectFile, "@PID@", pid);
	replaceStringInFile(objectFile, "@FILE@", contentPath);
	replaceStringInFile(objectFile, "@USERNAME@", FC_USERNAME);
	return objectFile;
    }

}
