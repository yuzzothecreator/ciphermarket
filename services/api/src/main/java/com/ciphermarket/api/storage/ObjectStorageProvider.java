package com.ciphermarket.api.storage;

import java.io.InputStream;

public interface ObjectStorageProvider {

    void putObject(String bucket, String key, InputStream data, long size, String contentType);

    InputStream getObject(String bucket, String key);

    void deleteObject(String bucket, String key);

    void moveObject(String sourceBucket, String sourceKey, String destBucket, String destKey);

    boolean objectExists(String bucket, String key);
}
