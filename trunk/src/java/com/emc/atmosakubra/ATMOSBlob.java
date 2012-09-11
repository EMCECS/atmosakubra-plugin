package com.emc.atmosakubra;

import com.emc.atmosakubra.utils.ATMOSConnection;
import com.emc.atmosakubra.utils.ATMOSOutputStream;
import com.emc.esu.api.ObjectPath;

import org.akubraproject.Blob;
import org.akubraproject.BlobStoreConnection;
import org.akubraproject.DuplicateBlobException;
import org.akubraproject.MissingBlobException;
import org.akubraproject.UnsupportedIdException;
import org.akubraproject.impl.AbstractBlob;
import org.akubraproject.impl.StreamManager;

import java.io.*;
import java.net.URI;
import java.util.Map;

/**
 * Atmos Blob implementation.
 */
public class ATMOSBlob extends AbstractBlob {
    private final ObjectPath objectPath;
    private StreamManager manager;
    private ATMOSConnection atmosConnection;

    protected ATMOSBlob(BlobStoreConnection connection, ATMOSConnection atmosConnection, URI blobId, StreamManager manager) {
	super(connection, blobId);
	this.atmosConnection = atmosConnection;
	objectPath = getPath(blobId);
	this.manager = manager;
    }

    private ObjectPath getPath(URI blobId) {
	if (blobId == null) {
	    throw new NullPointerException("Id cannot be null");
	}
	String path = new String(blobId.getRawSchemeSpecificPart().getBytes());
	return atmosConnection.getObjectPath(path);
    }

    /**
     * Opens a new InputStream for reading the content.
     * 
     * @return the input stream.
     * @throws IOException
     */
    public InputStream openInputStream() throws IOException {
	ensureOpen();
	if (!exists()) {
	    throw new MissingBlobException(getId());
	}
	
	return manager.manageInputStream(getConnection(), atmosConnection.getObjectInputStream(objectPath));
    }

    /**
     * Opens a new OutputStream for writing the content.
     * 
     * @param estimatedSize
     * @param overwrite
     * @return the output stream.
     * @throws IOException
     */
    public OutputStream openOutputStream(long estimatedSize, boolean overwrite) throws IOException {
	ensureOpen();
	if (!overwrite && exists())
	      throw new DuplicateBlobException(getId());
	atmosConnection.createObject(objectPath);
	ATMOSOutputStream managed = new ATMOSOutputStream(atmosConnection, objectPath);
	return manager.manageOutputStream(getConnection(), managed);
    }

    /**
     * Gets the size of the blob, in bytes.
     * 
     * @return the size in bytes, or -1 if unknown
     */
    public long getSize() {
	ensureOpen();
	return atmosConnection.getObjectSize(objectPath);
    }

    /**
     * Tests if a blob with this id exists in this blob-store.
     * 
     * @return true if the blob denoted by this id exists; false otherwise.
     */
    public boolean exists() {
	return getSize() != -1;
    }

    /**
     * Removes this blob from the store.
     */
    public void delete() {
	if (exists()) {
	    atmosConnection.delete(objectPath);
	}
    }

    /**
     * Move a blob object from one location to another
     * 
     * @param blobId
     *            the blob id of a new blobs location
     * @param hints
     * @return Blob in new location
     * @throws UnsupportedIdException
     */
    public Blob moveTo(URI blobId, Map<String, String> hints) {
	ensureOpen();
	if (exists()) {
	    ATMOSBlob newBlob = new ATMOSBlob(getConnection(), atmosConnection, blobId, manager);
	    atmosConnection.move(objectPath, newBlob.getPath(blobId));
	    return newBlob;
	}
	return null;
    }

}
