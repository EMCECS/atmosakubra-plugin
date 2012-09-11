package com.emc.atmosakubra.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.emc.atmosakubra.utils.ATMOSConnection;
import com.emc.esu.api.EsuException;
import com.emc.esu.api.ObjectPath;
import junit.framework.Assert;

public class MockATMOSConnection extends ATMOSConnection {

    private ArrayList<String> files = new ArrayList<String>();
    private String objData = "";

    @Override
    public InputStream getObjectInputStream(ObjectPath object) throws UnsupportedEncodingException {
	byte[] bytes;
	for (String file : files) {
	    if (file.equals(object.toString())) {
		bytes = objData.getBytes("UTF-8");
		return new ByteArrayInputStream(bytes);
	    }
	}

	return null;
    }

    public List<String> listAllObjects(String path) {
	    List<String> dirEntries = new ArrayList<String>();
        if (path.matches("[/]?")) {
            dirEntries.add("/A");
            dirEntries.add("/B");
            dirEntries.add("/C");
            dirEntries.add("/file4");
        }
        if (path.matches("/A[/]?")) {
            dirEntries.add("/A/file1");
            dirEntries.add("/A/file2");
        }
        if (path.matches("/C[/]?")) {
            dirEntries.add("/C/D");
        }
        if (path.matches("/C/D[/]?")) {
            dirEntries.add("/C/D/file3");
        }

        return dirEntries;
    }

    public boolean isFolder(ObjectPath object) throws EsuException {
        String path = object.toString();
        return path.matches("/testdir1[/]?")
                || path.matches("/testdir1/A[/]?")
                || path.matches("/testdir1/B[/]?")
                || path.matches("/testdir1/C[/]?")
                || path.matches("/testdir1/C/D[/]?");
    }

    public String createObject(String objectPath) {
	files.add(objectPath);
	return objectPath;
    }

    @Override
    public long getObjectSize(ObjectPath objectPath) {
	return files.contains(objectPath.toString())? objData.length() : -1;
    }

    @Override
    public void updateObject(ObjectPath objectPath, byte[] data, long dstOffset, int srcOffset, int size) {
	InputStream inputStream;
        try {
            inputStream = getObjectInputStream(objectPath);
            objData = convertStreamToString(inputStream) + convertStreamToString(new ByteArrayInputStream(Arrays.copyOf(data, size)));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }
    
    public String createObject(ObjectPath objectPath) {
	files.add(objectPath.toString());
	return objectPath.toString();
    }
    
    public String convertStreamToString(InputStream is) throws IOException {
        if (is != null) {
            Writer writer = new StringWriter();
            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                is.close();
            }
            return writer.toString();
        } else {
            return "";
        }
    }

    @Override
    public void delete(ObjectPath objectPath) {
	for (int i = 0; i < files.size(); i++) {
            if (files.get(i).equals(objectPath.toString())) {
                files.remove(i);
            }
        }
    }

    @Override
    public void move(ObjectPath srcPath, ObjectPath dstPath) {
	files.remove(srcPath.toString());
        files.add(dstPath.toString());
    }

}
