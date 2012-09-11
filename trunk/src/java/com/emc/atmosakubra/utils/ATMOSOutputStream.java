package com.emc.atmosakubra.utils;

import com.emc.esu.api.ObjectPath;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Wraps an <code>OutputStream</code> to provide notification to a
 * <code>CloseListener</code> when closed.
 */
public class ATMOSOutputStream extends OutputStream {
    protected static final Log LOG = LogFactory.getLog(ATMOSOutputStream.class);
    private boolean closed = false;
    byte[] buffer;
    private int bufferCurrentPos = 0;
    private ATMOSConnection atmosConnection;
    private ObjectPath objectPath;
    private long currentObjectSize = 0;

    /**
     * Creates an instance.
     * 
     * @param listener
     *            the CloseListener to notify when closed.
     * @param stream
     *            the stream to wrap.
     * @param con
     *            the store connection
     */
    public ATMOSOutputStream(ATMOSConnection connection, ObjectPath objectPath) {
	this.atmosConnection = connection;
	buffer = connection.createIOBuffer();
	this.objectPath = objectPath;
    }

    /**
     * Implement this far more efficiently than the ridiculous implementation in
     * the superclass.
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
	len = Math.min(b.length, len);
	off = Math.min(b.length - 1, off);
	for (int i = off; len > 0;) {
	    int chunkLen = Math.min(len, buffer.length - bufferCurrentPos);
	    System.arraycopy(b, i, buffer, bufferCurrentPos, chunkLen);
	    bufferCurrentPos += chunkLen;
	    if (bufferCurrentPos >= buffer.length - 1) {
		flush();
	    }
	    len -= chunkLen;
	    i += chunkLen;
	}
    }

    @Override
    public void flush() {
	if (bufferCurrentPos > 0) {
	    atmosConnection.updateObject(objectPath, buffer, currentObjectSize, 0, bufferCurrentPos);
	    currentObjectSize += bufferCurrentPos;
	    bufferCurrentPos = 0;
	}
    }

    /**
     * Closes the stream, then notifies the CloseListener and call sync method
     * of BlobStoreConnection to write data.
     */
    @Override
    public void close() throws IOException {
	LOG.debug("Closing output stream of object " + objectPath);
	if (!closed) {
	    flush();
	    closed = true;
	}
    }

    @Override
    public void write(int b) throws IOException {
	byte[] intValue = intToByteArray(b);
	write(intValue, 0, intValue.length);
    }

    protected byte[] intToByteArray(int value) {
	return new byte[] { (byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) value };
    }
}