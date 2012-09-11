package com.emc.atmosakubra;

import com.emc.atmosakubra.utils.ATMOSConnection;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Iterates over all files in baseDir.
 */
public class ATMOSBlobIdIterator implements Iterator<URI> {
    private ArrayList<Integer> entries = new ArrayList<Integer>();
    private Map<Integer, String> dirs = new HashMap<Integer, String>();
    private ATMOSConnection atmosConnection;

    /**
     * Creates an instance with the given ATMOSConnection
     *
     * @param atmosConnection
     */
    public ATMOSBlobIdIterator(ATMOSConnection atmosConnection) {
        this.atmosConnection = atmosConnection;
        entries.add(0);
        dirs.put(0, "/");
    }

    /**
     * @return true if the iteration has more elements.
     */
    public boolean hasNext() {
        String entry;
        int current, count;
        while (true) {
            current = entries.get(entries.size() - 1);
            count = atmosConnection.listAllObjects(dirs.get(dirs.size() - 1)).size();
            for (int i = current; i < count; i++) {
                entry = atmosConnection.listAllObjects(dirs.get(dirs.size() - 1)).get(i);
                if (atmosConnection.isFolder(atmosConnection.getObjectPath(entry))) {
                    i = -1;
                    dirs.put(dirs.size(), entry);
                    entries.add(0);
                    count = atmosConnection.listAllObjects(entry).size();
                } else {
                    return true;
                }
            }
            if (entries.size() > 1) {
                entries.set(entries.size() - 2, entries.get(entries.size() - 2) + 1);
            }
            entries.remove(entries.size() - 1);
            dirs.remove(dirs.size() - 1);
            if (entries.size() == 0) {
                return false;
            }
        }
    }

    /**
     * @return the next element in the iteration.
     */
    public URI next() {
        String entry;
        String currentDir = dirs.get(0);
        for (int i = 0; i < entries.size(); i++) {
            if (atmosConnection.listAllObjects(currentDir) == null || atmosConnection.listAllObjects(currentDir).size() - 1 < entries.get(i)) {
                if (i > 0) {
                    entries.set(i - 1, entries.get(i - 1) + 1);
                    dirs.remove(dirs.size() - 1);
                }
                entries.remove(i);
                if (entries.size() == 0) {
                    entries.add(0);
                    return null;
                }
                i = 0;
                currentDir = dirs.get(0);
            }
            entry = atmosConnection.listAllObjects(currentDir).get(entries.get(i));
            if (atmosConnection.isFolder(atmosConnection.getObjectPath(entry))) {
                if (i == entries.size() - 1) {
                    entries.add(0);
                    dirs.put(dirs.size(), entry);
                }
                currentDir = entry;
            } else {
                try {
                    entries.set(i, entries.get(i) + 1);
                    return new URI(entry.replace(dirs.get(0), "/"));
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * Removes from the underlying collection the last element returned by the iterator.
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }

}
