package com.jobintel.evidencing.service;

import java.io.InputStream;

public interface StorageService {

    String generateUploadUrl(String storageKey, String contentType);

    String generateDownloadUrl(String storageKey);

    void putObject(String storageKey, byte[] data, String contentType);

    InputStream getObject(String storageKey);

    boolean exists(String storageKey);
}
