package com.emc.atmosakubra;

import com.emc.atmosakubra.utils.ATMOSConnection;

import org.akubraproject.Blob;
import org.akubraproject.BlobStore;
import org.akubraproject.impl.AbstractBlobStoreConnection;
import org.akubraproject.impl.StreamManager;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.Map;

/**
 * Atmos BlobStoreConnection implementation.
 */
public class ATMOSBlobStoreConnection extends AbstractBlobStoreConnection {
    ATMOSConnection connection;
    private Blob blob;

    protected ATMOSBlobStoreConnection(ATMOSConnection connection, BlobStore blobStore, StreamManager manager) {
        super(blobStore, manager);
        this.connection = connection;
    }

    /**
     * Gets the blob with the given id.
     *
     * @param blobId the blob id
     * @param hints
     * @return Blob
     * @throws IOException
     * @throws UnsupportedOperationException
     */
    public Blob getBlob(URI blobId, Map<String, String> hints) throws IOException, UnsupportedOperationException {
        ensureOpen();
        if (blobId == null) {
            throw new UnsupportedOperationException();
        }
        blob = new ATMOSBlob(this, connection, blobId, streamManager);
        return blob;
    }

    /**
     * Gets an iterator over the ids of all blobs in this store.
     *
     * @param filterPrefix
     * @return Iterator<URI>
     * @throws IOException
     */
    public Iterator<URI> listBlobIds(String filterPrefix) throws IOException {
        ensureOpen();
        return new ATMOSBlobIdIterator(connection);
    }


    public void sync() throws IOException {
    }
    
    @Override
    public void close() {
      super.close();
    }

}
