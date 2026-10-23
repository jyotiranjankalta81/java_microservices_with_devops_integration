package com.jobintel.evidencing.service;

import java.util.UUID;

public interface QueueService {

    void enqueuePostPublication(UUID postId, String idempotencyKey);

    void enqueueNotificationDelivery(UUID postId);

    void processPublicationMessage(UUID postId, String idempotencyKey, int attempt);

    void processNotificationMessage(UUID postId, int attempt);
}
