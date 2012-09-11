package com.emc.atmosakubra.stress;

import com.emc.atmosakubra.BaseTest;

import org.fcrepo.apia.FedoraAPIA;
import org.fcrepo.apia.FedoraAPIAService;
import org.fcrepo.apim.FedoraAPIM;
import org.fcrepo.apim.FedoraAPIMService;

import javax.xml.ws.BindingProvider;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

public class BaseStressTest extends BaseTest {

    protected static final int SEC = 1000;
    protected static final int KB = 1024;
    protected static String FC_HOST = "localhost";
    protected static int FC_PORT = Integer.parseInt(System.getProperty("fedora.core.port", "8080"));
    protected static String FC_USERNAME = System.getProperty("fedora.core.username", "fedoraAdmin");
    protected static String FC_PASSWORD = System.getProperty("fedora.core.password");
    protected static int MAX_THREADS_NUMBER = 1000;
    protected static int THREADS_NUMBER = Math.min(MAX_THREADS_NUMBER,
	    Integer.parseInt(System.getProperty("stress.tests.threads.number", "1")));
    protected static int OBJECTS_NUMBER = Integer.parseInt(System.getProperty("stress.tests.objects.number", "10"));
    protected int OBJECT_DATASTREAM_SIZE = Integer.parseInt(System.getProperty(
	    "stress.tests.object.datastream.size", "2097152"));

    protected static FedoraAPIMService apiMService;
    protected static FedoraAPIAService apiAService;

    public BaseStressTest() throws MalformedURLException {
	apiAService = new FedoraAPIAService(FC_HOST, FC_PORT);
	apiMService = new FedoraAPIMService(FC_HOST, FC_PORT);
	System.gc();
	long freeMemory = Runtime.getRuntime().freeMemory();
	OBJECT_DATASTREAM_SIZE = (int)Math.min((freeMemory / THREADS_NUMBER) - (2 * 1024 * KB), OBJECT_DATASTREAM_SIZE);
    }

    public FedoraAPIM createAPIMInstance() {
	FedoraAPIM apim = apiMService.getFedoraAPIMServiceHTTPPort();
	Map<String, Object> requestContext = ((BindingProvider) apim).getRequestContext();
	requestContext.put(BindingProvider.USERNAME_PROPERTY, FC_USERNAME);
	requestContext.put(BindingProvider.PASSWORD_PROPERTY, FC_PASSWORD);
	return apim;
    }

    public FedoraAPIA createAPIAInstance() {
	FedoraAPIA apim = apiAService.getFedoraAPIAServiceHTTPPort();
	Map<String, Object> requestContext = ((BindingProvider) apim).getRequestContext();
	requestContext.put(BindingProvider.USERNAME_PROPERTY, FC_USERNAME);
	requestContext.put(BindingProvider.PASSWORD_PROPERTY, FC_PASSWORD);
	return apim;
    }

    protected double calculateStandardDeviation(List<Long> a) {
	long mean = calculateMean(a);
	long sum = 0;
	for (Long value : a) {
	    sum += Math.pow((value - mean), 2);
	}
	return Math.sqrt(sum / a.size());
    }

    protected long calculateMean(List<Long> a) {
	return calculateTotal(a) / a.size();
    }

    protected double calculateSpeed(long bytes, long timeMilliseconds) {
	return (bytes / KB) / ((double) timeMilliseconds / (double) SEC);
    }

    protected long calculateTotal(List<Long> a) {
	long sum = 0;
	for (Long value : a) {
	    sum += value;
	}
	return sum;
    }

}
