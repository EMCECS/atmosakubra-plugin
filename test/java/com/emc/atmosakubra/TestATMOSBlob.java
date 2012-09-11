package com.emc.atmosakubra;

import com.emc.atmosakubra.ATMOSBlobStoreConnection;
import com.emc.atmosakubra.utils.ATMOSConnection;
import com.emc.atmosakubra.utils.MockATMOSConnection;

import junit.framework.Assert;
import org.akubraproject.Blob;
import org.akubraproject.BlobStoreConnection;
import org.akubraproject.impl.StreamManager;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;

public class TestATMOSBlob {
    private BlobStoreConnection connection;

    public TestATMOSBlob() {
	ATMOSConnection atmosConnection = new MockATMOSConnection();
	atmosConnection.setBaseDir("/testdir1/");
        connection = new ATMOSBlobStoreConnection(atmosConnection, null, new StreamManager());
    }

    @Test
    public void testATMOSBlobDeleteObject() throws URISyntaxException, IOException {
	Blob blob = connection.getBlob(new URI("с4/testobj1"), null);
	OutputStream outputStream = blob.openOutputStream(0, false);
        outputStream.write("testData".getBytes());
        outputStream.close();
        Assert.assertEquals(8, blob.getSize());
        Assert.assertEquals(true, blob.exists());
        blob.delete();
        Assert.assertEquals(-1, blob.getSize());
        Assert.assertEquals(false, blob.exists());
    }

    @Test
    public void testATMOSBlobMoveObject() throws IOException, URISyntaxException {
        Blob blob = connection.getBlob(new URI("с4/testobj2"), null);
        OutputStream outputStream = blob.openOutputStream(0, false);
        outputStream.write("testData".getBytes());
        Blob blob2 = blob.moveTo(new URI("с4/testobj3"), null);
        Assert.assertEquals(false, blob.exists());
        Assert.assertEquals(true, blob2.exists());
    }

    @Test
    public void testATMOSBlobOutputStreamsWithObjectPath() throws IOException, URISyntaxException {
        Blob blob = connection.getBlob(new URI("с4/newobj"), null);
        Assert.assertEquals(false, blob.exists());
        OutputStream outputStream = blob.openOutputStream(0, false);
        outputStream.write("testData".getBytes());
        outputStream.close();
        Assert.assertEquals(true, blob.exists());
        testATMOSBlobInputStream(blob, "testData");
    }

    private void testATMOSBlobInputStream(Blob blob, String data) throws IOException, URISyntaxException {
        InputStream inputStream = blob.openInputStream();
        byte[] bytes = new byte[data.length()];
        inputStream.read(bytes);
        Assert.assertEquals(data, new String(bytes, "UTF-8"));
    }

    @Test
    public void testATMOSBlobIterator() throws IOException {
        Iterator<URI> iterator = connection.listBlobIds("");
        Assert.assertEquals(true, iterator.hasNext());
        Assert.assertEquals("/A/file1", iterator.next().getPath());
        Assert.assertEquals(true, iterator.hasNext());
        Assert.assertEquals("/A/file2", iterator.next().getPath());
        Assert.assertEquals(true, iterator.hasNext());
        Assert.assertEquals("/C/D/file3", iterator.next().getPath());
        Assert.assertEquals(true, iterator.hasNext());
        Assert.assertEquals("/file4", iterator.next().getPath());
        Assert.assertEquals(false, iterator.hasNext());
        Assert.assertNull(iterator.next());
    }

    @Test
    public void testATMOSBlobSyncFileUpdate() throws URISyntaxException, IOException {
        Blob blob = connection.getBlob(new URI("с4/testobj1"), null);
        OutputStream outputStream = blob.openOutputStream(0, false);
        outputStream.write("testData".getBytes());
        outputStream.write("Updated".getBytes());
        outputStream.close();
        testATMOSBlobInputStream(blob, "testDataUpdated");
    }

}
