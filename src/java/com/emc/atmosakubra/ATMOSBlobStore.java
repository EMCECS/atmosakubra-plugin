package com.emc.atmosakubra;

import com.emc.atmosakubra.utils.ATMOSConnection;

import org.akubraproject.BlobStoreConnection;
import org.akubraproject.impl.AbstractBlobStore;
import org.akubraproject.impl.StreamManager;

import javax.transaction.Transaction;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

/**
 * Atmos BlobStore implementation
 */
public class ATMOSBlobStore extends AbstractBlobStore {
    
    private ATMOSConnection atmosConnection;
    private StreamManager manager = new StreamManager();

    /**
     * Creates an instance with the given id, base storage directory and Atmos configuration
     *
     * @param id        the unique identifier of this blobstore.
     * @param pluginConfiguration configuration parameters of the plugin
     */
    public ATMOSBlobStore(URI id, ATMOSConnection atmosConnection) {
        super(id);
        this.atmosConnection = atmosConnection;
    }

    /**
     * Opens a connection to the blob store.
     *
     * @param tx
     * @param hints
     * @return BlobStoreConnection
     * @throws UnsupportedOperationException
     * @throws IOException
     */
    public BlobStoreConnection openConnection(Transaction tx, Map<String, String> hints) throws UnsupportedOperationException, IOException {
        return new ATMOSBlobStoreConnection(atmosConnection, this, manager);
    }

}
