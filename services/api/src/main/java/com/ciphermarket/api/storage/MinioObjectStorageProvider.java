package com.ciphermarket.api.storage;

import com.ciphermarket.api.config.StorageProperties;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class MinioObjectStorageProvider implements ObjectStorageProvider {

    private final MinioClient minioClient;

    public MinioObjectStorageProvider(StorageProperties properties) {
        this.minioClient = MinioClient.builder()
                .endpoint(properties.endpoint())
                .credentials(properties.accessKey(), properties.secretKey())
                .build();
    }

    @Override
    public void putObject(String bucket, String key, InputStream data, long size, String contentType) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(key)
                            .stream(data, size, -1)
                            .contentType(contentType)
                            .build()
            );
        } catch (Exception ex) {
            throw new StorageException("Failed to store object: " + key, ex);
        }
    }

    @Override
    public InputStream getObject(String bucket, String key) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder().bucket(bucket).object(key).build()
            );
        } catch (Exception ex) {
            throw new StorageException("Failed to read object: " + key, ex);
        }
    }

    @Override
    public void deleteObject(String bucket, String key) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder().bucket(bucket).object(key).build()
            );
        } catch (Exception ex) {
            throw new StorageException("Failed to delete object: " + key, ex);
        }
    }

    @Override
    public void moveObject(String sourceBucket, String sourceKey, String destBucket, String destKey) {
        throw new UnsupportedOperationException("Use copy-then-delete via processing pipeline");
    }

    @Override
    public boolean objectExists(String bucket, String key) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder().bucket(bucket).object(key).build()
            );
            return true;
        } catch (ErrorResponseException ex) {
            if ("NoSuchKey".equals(ex.errorResponse().code())) {
                return false;
            }
            throw new StorageException("Failed to stat object: " + key, ex);
        } catch (Exception ex) {
            throw new StorageException("Failed to stat object: " + key, ex);
        }
    }
}
