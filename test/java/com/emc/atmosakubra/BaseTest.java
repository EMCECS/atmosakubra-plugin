package com.emc.atmosakubra;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fcrepo.apim.FedoraAPIM;
import org.junit.After;
import org.junit.Before;

public class BaseTest {

    private static final Log LOG = LogFactory.getLog(BaseTest.class);
    private Set<File> filesToRemove = new HashSet<File>();
    protected File temporaryFolder;

    @Before
    public void startUp() throws Exception {
	temporaryFolder = File.createTempFile(UUID.randomUUID().toString(), "");
	temporaryFolder.delete();
	temporaryFolder.mkdir();
	deleteOnExit(temporaryFolder);
    }

    @After
    public void cleanUp() throws Exception {
	Set<File> filesToRemoveRepeatedly = new HashSet<File>();
	for (File file : filesToRemove) {
	    filesToRemoveRepeatedly.addAll(removeRecursively(file));
	}
	for (File file : filesToRemoveRepeatedly) {
	    if (file.exists())
		if (!file.delete()) {
		    LOG.error("could not remove file " + file.getCanonicalPath());
		}
	}
    }

    private Set<File> removeRecursively(File file) {
	Set<File> filesToRemoveRepeatedly = new HashSet<File>();
	if (file.exists()) {
	    if (file.isDirectory()) {
		for (String fileInFolder : file.list()) {
		    filesToRemoveRepeatedly.addAll(removeRecursively(new File(file, fileInFolder)));
		}
	    }
	    if (!file.delete())
		filesToRemoveRepeatedly.add(file);
	}
	return filesToRemoveRepeatedly;
    }

    public static File getResource(String name, Class<?> clazz) throws IOException {
	String packageName = clazz.getPackage().getName();
	ClassLoader cLoader = clazz.getClassLoader();
	URL resource = cLoader.getResource(packageName.replaceAll("[.]", File.separator) + File.separator + name);
	if (resource == null)
	    resource = cLoader.getResource(name);
	if (resource == null)
	    throw new IOException("no such resource " + packageName.replaceAll("[.]", File.separator) + File.separator
		    + name);
	File f = new File(resource.getFile());
	if (f.exists())
	    return f;
	else
	    throw new IOException("File " + name + " not found in " + packageName);
    }

    public boolean compare(File expected, File actual) throws IOException {
	InputStream extectedIn = null, actualIn = null;
	try {
	    byte[] extectedBuffer = new byte[4096];
	    byte[] actualBuffer = new byte[4096];
	    extectedIn = new FileInputStream(expected);
	    actualIn = new FileInputStream(actual);
	    int expectedSize, actualSize;
	    do {
		expectedSize = extectedIn.read(extectedBuffer);
		actualSize = actualIn.read(actualBuffer);
		if (expectedSize != actualSize) {
		    return false;
		}
		if(expectedSize > 0){
		    for (expectedSize--; expectedSize >= 0; expectedSize--) {
			if (extectedBuffer[expectedSize] != actualBuffer[expectedSize])
			    return false;
		    }
		}
	    } while (expectedSize != -1 && actualSize != -1);
	} catch (IOException e) {
	    return false;
	} finally {
	    if (extectedIn != null)
		extectedIn.close();
	    if (actual != null)
		actualIn.close();
	}
	return true;
    }

    public File createFileFromBuffer(byte[] buffer) throws IOException {
	File file = new File(temporaryFolder, UUID.randomUUID().toString() + "-file");
	file.createNewFile();
	deleteOnExit(file);
	OutputStream out = null;
	try {
	    out = new FileOutputStream(file);
	    for (int i = 0; i < buffer.length; i++) {
		out.write(buffer[i]);
	    }
	} catch (IOException e) {
	    throw e;
	} finally {
	    if (out != null)
		out.close();
	}
	return file;
    }

    public void copyfile(File src, File dst) throws IOException {
	try {
	    InputStream in = new FileInputStream(src);
	    OutputStream out = new FileOutputStream(dst);

	    byte[] buf = new byte[1024 * 1024];
	    int len;
	    while ((len = in.read(buf)) > 0) {
		out.write(buf, 0, len);
	    }
	    in.close();
	    out.close();
	} catch (FileNotFoundException e) {
	    LOG.error("File not found", e);
	} catch (IOException e) {
	    LOG.error("Could not copy file " + src.getCanonicalPath() + " to " + dst.getCanonicalPath(), e);
	}
    }

    public void replaceStringInFile(File file, String placeholder, String replaceWith) throws IOException {
	File tmpFile = new File(temporaryFolder, UUID.randomUUID().toString());
	BufferedReader in = new BufferedReader(new FileReader(file));
	PrintWriter out = new PrintWriter(tmpFile);
	try {
	    String line = null;
	    do {
		line = in.readLine();
		if (line != null) {
		    line = line.replaceAll(placeholder, replaceWith);
		    out.println(line);
		}
	    } while (line != null);
	} finally {
	    in.close();
	    out.close();
	}
	if (file.delete()) {
	    tmpFile.renameTo(file);
	}
    }

    public void deleteOnExit(File file) {
	filesToRemove.add(file);
    }

    public String ingest(FedoraAPIM apim, InputStream ingestStream, String ingestFormat, String logMessage)
	    throws RemoteException, IOException {
	ByteArrayOutputStream out = new ByteArrayOutputStream();
	pipeStream(ingestStream, out, 4096);
	String pid = apim.ingest(out.toByteArray(), ingestFormat, logMessage);
	return pid;
    }

    public static void pipeStream(InputStream in, OutputStream out, int bufSize) throws IOException {
	try {
	    byte[] buf = new byte[bufSize];
	    int len;
	    while ((len = in.read(buf)) > 0) {
		out.write(buf, 0, len);
	    }
	} finally {
	    try {
		in.close();
		out.close();
	    } catch (IOException e) {
		System.err.println("WARNING: Could not close stream.");
	    }
	}
    }

    public String purgeObject(FedoraAPIM apim, String pid, String logMessage, boolean force) throws RemoteException {
	String purgeDateTime = apim.purgeObject(pid, logMessage, force);
	return purgeDateTime;
    }

    public File createFileWithRandomContent(long size) throws IOException {
	File temporaryFile = new File(temporaryFolder, UUID.randomUUID().toString());
	temporaryFile.createNewFile();
	deleteOnExit(temporaryFile);
	FileOutputStream out = new FileOutputStream(temporaryFile);
	int bufferSize = (int)Math.min(4 * 1024, size);
	byte[] buffer = new byte[bufferSize];
	Random random = new Random();
	try {
	    while (size >= 0) {
		random.nextBytes(buffer);
		out.write(buffer);
		size -= bufferSize;
	    }
	} finally {
	    out.close();
	}
	return temporaryFile;
    }

}
