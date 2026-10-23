package com.jobintel.evidencing.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class S3StorageService implements StorageService {

    private final String bucketName;
    private final Duration urlExpiration;
    private final String localStoragePath;
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String signingSecret;
    private final ConcurrentHashMap<String, byte[]> inMemoryStorage = new ConcurrentHashMap<>();

    public S3StorageService(
            @Value("${aws.region:us-east-1}") String regionStr,
            @Value("${aws.endpoint:}") String endpointStr,
            @Value("${aws.s3.bucket:evidencing-media-bucket}") String bucketName,
            @Value("${aws.s3.url-expiration-minutes:15}") int urlExpirationMinutes,
            @Value("${aws.s3.local-storage-path:./target/evidencing-storage}") String localStoragePath,
            @Value("${jwt.secret:default-signing-secret-key-32-chars-long!!}") String signingSecret) {

        this.bucketName = bucketName;
        this.urlExpiration = Duration.ofMinutes(urlExpirationMinutes);
        this.localStoragePath = localStoragePath;
        this.signingSecret = signingSecret;

        S3Client client = null;
        S3Presigner presigner = null;

        try {
            Region region = Region.of(regionStr);
            var clientBuilder = S3Client.builder().region(region);
            var presignerBuilder = S3Presigner.builder().region(region);

            if (endpointStr != null && !endpointStr.isBlank()) {
                URI endpoint = URI.create(endpointStr);
                clientBuilder.endpointOverride(endpoint).forcePathStyle(true);
                presignerBuilder.endpointOverride(endpoint);
                var creds = StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test"));
                clientBuilder.credentialsProvider(creds);
                presignerBuilder.credentialsProvider(creds);
            }

            client = clientBuilder.build();
            presigner = presignerBuilder.build();
            log.info("Initialized AWS S3 Client and Presigner for bucket: {}", bucketName);
        } catch (Exception e) {
            log.warn("AWS S3 client init warning: {}. Falling back to local storage.", e.getMessage());
        }

        this.s3Client = client;
        this.s3Presigner = presigner;

        // Ensure local storage directory exists
        try {
            Files.createDirectories(Paths.get(localStoragePath));
        } catch (Exception ignored) {
        }
    }

    @Override
    public String generateUploadUrl(String storageKey, String contentType) {
        if (s3Presigner != null && s3Client != null) {
            try {
                PutObjectRequest objectRequest = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(storageKey)
                        .contentType(contentType)
                        .build();

                PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                        .signatureDuration(urlExpiration)
                        .putObjectRequest(objectRequest)
                        .build();

                PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
                return presignedRequest.url().toString();
            } catch (Exception e) {
                log.warn("Failed to generate S3 presigned PUT URL, using local endpoint: {}", e.getMessage());
            }
        }

        // Local fallback: generate signed local upload URL
        long expiryEpoch = Instant.now().plus(urlExpiration).getEpochSecond();
        String token = computeHmacToken(storageKey, expiryEpoch);
        return String.format("/api/evidencing/media/upload-direct?key=%s&expires=%d&token=%s",
                storageKey, expiryEpoch, token);
    }

    @Override
    public String generateDownloadUrl(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            return null;
        }

        // Return direct link if already an absolute external web URL (e.g. Unsplash demo image)
        if (storageKey.startsWith("http://") || storageKey.startsWith("https://")) {
            return storageKey;
        }

        if (s3Presigner != null && s3Client != null) {
            try {
                GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(storageKey)
                        .build();

                GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                        .signatureDuration(urlExpiration)
                        .getObjectRequest(getObjectRequest)
                        .build();

                PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(getObjectPresignRequest);
                return presignedGetObjectRequest.url().toString();
            } catch (Exception e) {
                log.warn("Failed to generate S3 presigned GET URL, using local endpoint: {}", e.getMessage());
            }
        }

        // Local fallback: generate short-lived signed GET URL
        long expiryEpoch = Instant.now().plus(urlExpiration).getEpochSecond();
        String token = computeHmacToken(storageKey, expiryEpoch);
        return String.format("/api/evidencing/media/file?key=%s&expires=%d&token=%s",
                storageKey, expiryEpoch, token);
    }

    @Override
    public void putObject(String storageKey, byte[] data, String contentType) {
        inMemoryStorage.put(storageKey, data);

        // Also persist to local file path
        try {
            Path target = Paths.get(localStoragePath, storageKey.replace("/", "_"));
            Files.write(target, data);
        } catch (Exception e) {
            log.debug("Local file write: {}", e.getMessage());
        }

        if (s3Client != null) {
            try {
                PutObjectRequest request = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(storageKey)
                        .contentType(contentType)
                        .build();
                s3Client.putObject(request, RequestBody.fromBytes(data));
            } catch (Exception e) {
                log.warn("S3 putObject error: {}", e.getMessage());
            }
        }
    }

    @Override
    public InputStream getObject(String storageKey) {
        byte[] inMem = inMemoryStorage.get(storageKey);
        if (inMem != null) {
            return new ByteArrayInputStream(inMem);
        }

        try {
            Path target = Paths.get(localStoragePath, storageKey.replace("/", "_"));
            if (Files.exists(target)) {
                return Files.newInputStream(target);
            }
        } catch (Exception ignored) {
        }

        if (s3Client != null) {
            try {
                GetObjectRequest request = GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(storageKey)
                        .build();
                return s3Client.getObject(request);
            } catch (Exception e) {
                log.warn("S3 getObject error: {}", e.getMessage());
            }
        }

        return null;
    }

    @Override
    public boolean exists(String storageKey) {
        if (inMemoryStorage.containsKey(storageKey)) return true;
        Path target = Paths.get(localStoragePath, storageKey.replace("/", "_"));
        return Files.exists(target);
    }

    public boolean verifyHmacToken(String storageKey, long expiresEpoch, String token) {
        if (Instant.now().getEpochSecond() > expiresEpoch) {
            return false; // Expired
        }
        String expected = computeHmacToken(storageKey, expiresEpoch);
        return expected.equals(token);
    }

    private String computeHmacToken(String storageKey, long expiresEpoch) {
        try {
            String data = storageKey + ":" + expiresEpoch;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(signingSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(rawHmac);
        } catch (Exception e) {
            return "signature-error";
        }
    }
}
