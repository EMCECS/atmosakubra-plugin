package com.emc.atmosakubra.functional;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.xml.ws.BindingProvider;

import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.fcrepo.apia.FedoraAPIA;
import org.fcrepo.apia.FedoraAPIAService;
import org.fcrepo.apim.FedoraAPIM;
import org.fcrepo.apim.FedoraAPIMService;

import com.emc.atmosakubra.BaseTest;
import com.emc.atmosakubra.utils.ATMOSConnection;
import com.emc.atmosakubra.utils.Utils;

public class BaseFunctionalTest extends BaseTest {

    protected static FedoraAPIMService apiMService;
    protected static FedoraAPIAService apiAService;
    protected static String fedoraUser = System.getProperty("fedora.user", "fedoraAdmin");
    protected static String fedoraPassword = System.getProperty("fedora.password", "password");
    protected static String fcRepositoryBaseDir = System.getProperty("functional.tests.basedir");
    protected static String atmosHost = System.getProperty("functional.tests.atmos.host");
    protected static int atmosPort = Integer.parseInt(System.getProperty("functional.tests.atmos.port", "-1"));
    protected static String atmosUID = System.getProperty("functional.tests.atmos.uid");
    protected static String atmosSecret = System.getProperty("functional.tests.atmos.secret");
    private static Server jettyServerWithFC;

    public BaseFunctionalTest() throws Exception {
	checkJVMProperiesAreSet();
	cleanUpBaseDir();
	if (jettyServerWithFC == null) {
	    jettyServerWithFC = new Server(8888);
	    WebAppContext webapp = new WebAppContext();
	    webapp.setContextPath("/fedora");
	    File warFile = getResource("fc-webapp", BaseFunctionalTest.class);
	    File securityRealmFile = getResource("fedora-home/security.realm", BaseFunctionalTest.class);
	    webapp.setResourceBase(warFile.getCanonicalPath());
	    webapp.setParentLoaderPriority(true);
	    webapp.setClassLoader(BaseFunctionalTest.class.getClassLoader());
	    webapp.getSecurityHandler().setLoginService(
		    new HashLoginService("FC Realm", securityRealmFile.getCanonicalPath()));
	    jettyServerWithFC.setHandler(webapp);
	    jettyServerWithFC.start();
	    apiMService = new FedoraAPIMService();
	    apiAService = new FedoraAPIAService();
	}
    }

    protected void checkJVMProperiesAreSet() throws IOException {
	if (fcRepositoryBaseDir == null || "".equals(fcRepositoryBaseDir.trim())){
	    throw new IOException("build property 'functional.tests.basedir' is not set");
	} else{
	    fcRepositoryBaseDir = Utils.joinTwoPaths("/", Utils.joinTwoPaths(fcRepositoryBaseDir, "/"));
	}
	if (atmosHost == null || "".equals(atmosHost.trim()))
	    throw new IOException("build property 'functional.tests.atmos.host' is not set");
	if (atmosPort == -1)
	    throw new IOException("build property 'functional.tests.atmos.port' is not set");
	if (atmosUID == null || "".equals(atmosUID.trim()))
	    throw new IOException("build property 'functional.tests.atmos.uid' is not set");
	if (atmosSecret == null || "".equals(atmosSecret.trim()))
	    throw new IOException("build property 'functional.tests.atmos.secret' is not set");
    }

    public FedoraAPIM createAPIMInstance() {
	FedoraAPIM apim = apiMService.getFedoraAPIMServiceHTTPPort();
	Map<String, Object> requestContext = ((BindingProvider) apim).getRequestContext();
	requestContext.put(BindingProvider.USERNAME_PROPERTY, fedoraUser);
	requestContext.put(BindingProvider.PASSWORD_PROPERTY, fedoraPassword);
	return apim;
    }

    public FedoraAPIA createAPIAInstance() {
	FedoraAPIA apim = apiAService.getFedoraAPIAServiceHTTPPort();
	Map<String, Object> requestContext = ((BindingProvider) apim).getRequestContext();
	requestContext.put(BindingProvider.USERNAME_PROPERTY, fedoraUser);
	requestContext.put(BindingProvider.PASSWORD_PROPERTY, fedoraPassword);
	return apim;
    }

    public void cleanUpBaseDir() {
	ATMOSConnection connection = new ATMOSConnection(atmosHost, atmosPort, atmosUID, atmosSecret);
	connection.deleteRecursively(fcRepositoryBaseDir);
    }

}
