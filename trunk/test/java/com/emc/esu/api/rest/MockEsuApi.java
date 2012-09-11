package com.emc.esu.api.rest;

import com.emc.esu.api.*;

import java.io.*;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MockEsuApi implements EsuApi {
    private ArrayList<String> files = new ArrayList<String>();
    private String objData = "testData";

    public MockEsuApi() {
        files.add("/testdir1/с4/testobj1");
        files.add("/testdir1/с4/testobj2");
    }

    public void setUid(String uid) {

    }

    public void setSecret(String sharedSecret) {

    }

    public ObjectId createObject(Acl acl, MetadataList metadata, byte[] data, String mimeType) {
        return null;
    }

    public ObjectId createObject(Acl acl, MetadataList metadata, byte[] data, String mimeType, Checksum checksum) {
        return null;
    }

    public ObjectId createObjectFromStream(Acl acl, MetadataList metadata, InputStream data, long length, String mimeType) {
        return null;
    }

    public ObjectId createObjectOnPath(ObjectPath path, Acl acl, MetadataList metadata, byte[] data, String mimeType) {
        files.add(path.toString());
        return null;
    }

    public ObjectId createObjectOnPath(ObjectPath path, Acl acl, MetadataList metadata, byte[] data, String mimeType, Checksum checksum) {
        return null;
    }

    public ObjectId createObjectFromSegment(Acl acl, MetadataList metadata, BufferSegment data, String mimeType) {
        return null;
    }

    public ObjectId createObjectFromSegment(Acl acl, MetadataList metadata, BufferSegment data, String mimeType, Checksum checksum) {
        return null;
    }

    public ObjectId createObjectFromSegmentOnPath(ObjectPath path, Acl acl, MetadataList metadata, BufferSegment data, String mimeType) {
        return null;
    }

    public ObjectId createObjectFromSegmentOnPath(ObjectPath path, Acl acl, MetadataList metadata, BufferSegment data, String mimeType, Checksum checksum) {
        return null;
    }

    public void updateObject(Identifier id, Acl acl, MetadataList metadata, Extent extent, byte[] data, String mimeType) {

    }

    public void updateObject(Identifier id, Acl acl, MetadataList metadata, Extent extent, byte[] data, String mimeType, Checksum checksum) {

    }

    public MessageDigest updateObjectFromStream(Identifier id, Acl acl, MetadataList metadata, Extent extent, InputStream data, long length, String mimeType) {
        InputStream inputStream;
        try {
            inputStream = readObjectStream(id, extent);
            objData = convertStreamToString(inputStream) + convertStreamToString(data);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
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

    public MessageDigest updateObjectFromStream(Identifier id, Acl acl, MetadataList metadata, Extent extent, InputStream data, long length, String mimeType, MessageDigest messageDigest) {
        return null;
    }

    public void updateObjectFromSegment(Identifier id, Acl acl, MetadataList metadata, Extent extent, BufferSegment data, String mimeType) {

    }

    public void updateObjectFromSegment(Identifier id, Acl acl, MetadataList metadata, Extent extent, BufferSegment data, String mimeType, Checksum checksum) {

    }

    public void setUserMetadata(Identifier id, MetadataList metadata) {

    }

    public void setAcl(Identifier id, Acl acl) {

    }

    public void deleteObject(Identifier id) {
        for (int i = 0; i < files.size(); i++) {
            if (files.get(i).equals(id.toString())) {
                files.remove(i);
            }
        }
    }

    public void deleteVersion(ObjectId id) {

    }

    public MetadataList getUserMetadata(Identifier id, MetadataTags tags) {
        return null;
    }

    public MetadataList getSystemMetadata(Identifier id, MetadataTags tags) {
	MetadataList list = new MetadataList(){
	    {
		addMetadata(new Metadata("size", "8", true));
	    }
	};
        return list;
    }

    public byte[] readObject(Identifier id, Extent extent, byte[] buffer) {
        return new byte[0];
    }

    public byte[] readObject(String host, int port, Identifier id, Extent extent, byte[] buffer, Checksum checksum) {
        return new byte[0];
    }

    public byte[] readObject(String host, int port, Identifier id, Extent extent, byte[] buffer) {
        return new byte[0];
    }

    public byte[] readObject(Identifier id, Extent extent, byte[] buffer, Checksum checksum) {
        return new byte[0];
    }

    public InputStream readObjectStream(Identifier id, Extent extent) throws UnsupportedEncodingException {
        byte[] bytes;
        for (String file : files) {
            if (file.equals(id.toString())) {
                MetadataList list = new MetadataList();
                Metadata fileSize = new Metadata("size", "100", true);
                list.addMetadata(fileSize);
                bytes = objData.getBytes("UTF-8");
                return new ByteArrayInputStream(bytes);
            }
        }

        return null;
    }

    public Acl getAcl(Identifier id) {
        return new Acl();
    }

    public void deleteUserMetadata(Identifier id, MetadataTags tags) {

    }

    public List<Identifier> listVersions(Identifier id) {
        return null;
    }

    public ObjectId versionObject(Identifier id) {
        return null;
    }

    public List<Identifier> listObjects(MetadataTag tag) {
        return null;
    }

    public List<Identifier> listObjects(String tag) {
        return null;
    }

    public List<ObjectResult> listObjectsWithMetadata(MetadataTag tag) {
        return null;
    }

    public List<ObjectResult> listObjectsWithMetadata(String tag) {
        return null;
    }

    public MetadataTags getListableTags(MetadataTag tag) {
        return null;
    }

    public MetadataTags getListableTags(String tag) {
        return null;
    }

    public MetadataTags listUserMetadataTags(Identifier id) {
        return null;
    }

    public List<Identifier> queryObjects(String xquery) {
        return null;
    }

    public List<DirectoryEntry> listDirectory(ObjectPath path) {
        List<DirectoryEntry> dirEntries = new ArrayList<DirectoryEntry>();
        DirectoryEntry entry = new DirectoryEntry();
        entry.setPath(new ObjectPath("obj1"));
        dirEntries.add(entry);

        DirectoryEntry entry2 = new DirectoryEntry();
        entry2.setPath(new ObjectPath("obj2"));
        dirEntries.add(entry2);
        return dirEntries;
    }

    public ObjectMetadata getAllMetadata(Identifier id) {
        return new ObjectMetadata();
    }

    public URL getShareableUrl(Identifier id, Date expiration) {
        return null;
    }

    public void rename(ObjectPath source, ObjectPath destination, boolean force) {
        files.remove(source.toString());
        files.add(destination.toString());
    }

    public void restoreVersion(ObjectId id, ObjectId vId) {

    }

    public ServiceInformation getServiceInformation() {
        return null;
    }

    public ObjectInfo getObjectInfo(Identifier id, boolean includeLayoutInfo) {
        return null;
    }

}
