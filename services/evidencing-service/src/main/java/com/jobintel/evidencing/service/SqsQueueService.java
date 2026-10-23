package com.jobintel.evidencing.service;

import com.jobintel.evidencing.domain.entity.*;
import com.jobintel.evidencing.domain.enums.PublicationStatus;
import com.jobintel.evidencing.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SqsQueueService implements QueueService {

    private final String publicationQueue;
    private final String publicationDlq;
    private final String notificationsQueue;
    private final String notificationsDlq;
    private final int maxRetries;
    private final SqsClient sqsClient;

    private final PostRepository postRepository;
    private final PublicationDlqEntryRepository dlqEntryRepository;
    private final NotificationService notificationService;

    // Track processed publication idempotency keys in memory as well
    private final ConcurrentHashMap<String, Boolean> processedPublicationKeys = new ConcurrentHashMap<>();

    public SqsQueueService(
            @Value("${aws.region:us-east-1}") String regionStr,
            @Value("${aws.endpoint:}") String endpointStr,
            @Value("${aws.sqs.publication-queue:evidencing-post-publication-queue}") String publicationQueue,
            @Value("${aws.sqs.publication-dlq:evidencing-post-publication-dlq}") String publicationDlq,
            @Value("${aws.sqs.notifications-queue:evidencing-notifications-queue}") String notificationsQueue,
            @Value("${aws.sqs.notifications-dlq:evidencing-notifications-dlq}") String notificationsDlq,
            @Value("${aws.sqs.max-retry-attempts:3}") int maxRetries,
            PostRepository postRepository,
            PublicationDlqEntryRepository dlqEntryRepository,
            NotificationService notificationService) {

        this.publicationQueue = publicationQueue;
        this.publicationDlq = publicationDlq;
        this.notificationsQueue = notificationsQueue;
        this.notificationsDlq = notificationsDlq;
        this.maxRetries = maxRetries;
        this.postRepository = postRepository;
        this.dlqEntryRepository = dlqEntryRepository;
        this.notificationService = notificationService;

        SqsClient client = null;
        try {
            Region region = Region.of(regionStr);
            var builder = SqsClient.builder().region(region);
            if (endpointStr != null && !endpointStr.isBlank()) {
                builder.endpointOverride(URI.create(endpointStr));
                builder.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test")));
            }
            client = builder.build();
            log.info("Initialized AWS SQS client for queues: {} / {}", publicationQueue, notificationsQueue);
        } catch (Exception e) {
            log.warn("SQS Client init warning: {}. Operating in resilient local worker mode.", e.getMessage());
        }
        this.sqsClient = client;
    }

    @Override
    @Async
    public void enqueuePostPublication(UUID postId, String idempotencyKey) {
        log.info("Enqueuing post publication for post {} with key {}", postId, idempotencyKey);
        if (sqsClient != null) {
            try {
                sqsClient.sendMessage(SendMessageRequest.builder()
                        .queueUrl(publicationQueue)
                        .messageBody(String.format("{\"postId\":\"%s\",\"idempotencyKey\":\"%s\"}", postId, idempotencyKey))
                        .build());
            } catch (Exception e) {
                log.warn("SQS publish error: {}. Processing locally.", e.getMessage());
            }
        }
        // Asynchronous worker consumer
        processPublicationMessage(postId, idempotencyKey, 1);
    }

    @Override
    @Async
    public void enqueueNotificationDelivery(UUID postId) {
        log.info("Enqueuing notification delivery for post {}", postId);
        if (sqsClient != null) {
            try {
                sqsClient.sendMessage(SendMessageRequest.builder()
                        .queueUrl(notificationsQueue)
                        .messageBody(String.format("{\"postId\":\"%s\"}", postId))
                        .build());
            } catch (Exception e) {
                log.warn("SQS notification publish error: {}. Processing locally.", e.getMessage());
            }
        }
        // Asynchronous worker consumer
        processNotificationMessage(postId, 1);
    }

    @Override
    @Transactional
    public void processPublicationMessage(UUID postId, String idempotencyKey, int attempt) {
        if (idempotencyKey != null && processedPublicationKeys.containsKey(idempotencyKey)) {
            log.info("Idempotent check: Key {} already processed. Skipping.", idempotencyKey);
            return;
        }

        try {
            Post post = postRepository.findById(postId).orElse(null);
            if (post == null) {
                log.warn("Post {} not found during publication processing", postId);
                return;
            }

            if (post.getPublicationStatus() == PublicationStatus.PUBLISHED) {
                log.info("Post {} already published. Idempotent return.", postId);
                if (idempotencyKey != null) processedPublicationKeys.put(idempotencyKey, true);
                return;
            }

            // Transition to PUBLISHED
            post.setPublicationStatus(PublicationStatus.PUBLISHED);
            post.setPublishedAt(Instant.now());
            postRepository.save(post);

            if (idempotencyKey != null) {
                processedPublicationKeys.put(idempotencyKey, true);
            }

            log.info("Post {} successfully published via queue consumer.", postId);

            // Queue notification delivery
            enqueueNotificationDelivery(postId);

        } catch (Exception e) {
            log.error("Error processing publication for post {} (attempt {}): {}", postId, attempt, e.getMessage());
            if (attempt < maxRetries) {
                processPublicationMessage(postId, idempotencyKey, attempt + 1);
            } else {
                log.error("Exhausted retries for post publication {}. Sending to DLQ.", postId);
                PublicationDlqEntry dlq = PublicationDlqEntry.builder()
                        .queueName(publicationDlq)
                        .payload(String.format("{\"postId\":\"%s\",\"idempotencyKey\":\"%s\"}", postId, idempotencyKey))
                        .errorReason(e.getMessage() != null ? e.getMessage() : e.getClass().getName())
                        .retryCount(attempt)
                        .build();
                dlqEntryRepository.save(dlq);

                // Set post to failed if fatal
                postRepository.findById(postId).ifPresent(p -> {
                    p.setPublicationStatus(PublicationStatus.FAILED);
                    postRepository.save(p);
                });
            }
        }
    }

    @Override
    @Transactional
    public void processNotificationMessage(UUID postId, int attempt) {
        try {
            notificationService.generateAndSendNotifications(postId);
            log.info("Notifications successfully dispatched for post {}", postId);
        } catch (Exception e) {
            log.error("Error dispatching notifications for post {} (attempt {}): {}", postId, attempt, e.getMessage());
            if (attempt < maxRetries) {
                processNotificationMessage(postId, attempt + 1);
            } else {
                log.error("Exhausted retries for notifications for post {}. Sending to DLQ.", postId);
                PublicationDlqEntry dlq = PublicationDlqEntry.builder()
                        .queueName(notificationsDlq)
                        .payload(String.format("{\"postId\":\"%s\"}", postId))
                        .errorReason(e.getMessage() != null ? e.getMessage() : e.getClass().getName())
                        .retryCount(attempt)
                        .build();
                dlqEntryRepository.save(dlq);
            }
        }
    }
}
