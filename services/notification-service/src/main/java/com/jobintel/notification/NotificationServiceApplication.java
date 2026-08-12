package com.jobintel.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@SpringBootApplication
@EnableKafka
@ComponentScan(basePackages = {"com.jobintel.notification", "com.jobintel.common"})
public class NotificationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }

    @Component
    @RequiredArgsConstructor
    static class NotificationConsumer {
        private final ObjectMapper objectMapper;

        @KafkaListener(topics = {"export.completed", "export.failed"}, groupId = "notification-service")
        public void onExportEvent(String message) {
            log.info("Notification event received: {}", message);
        }
    }
}
