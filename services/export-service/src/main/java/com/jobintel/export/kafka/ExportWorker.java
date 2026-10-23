package com.jobintel.export.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobintel.common.events.KafkaTopics;
import com.jobintel.export.domain.ExportJob;
import com.jobintel.export.repository.ExportJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExportWorker {

    private final ExportJobRepository repository;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final WebClient.Builder webClientBuilder;

    @Value("${job.service.url:http://localhost:8082}")
    private String jobServiceUrl;

    @Value("${export.storage.path:/tmp/exports}")
    private String storagePath;

    @KafkaListener(topics = KafkaTopics.EXPORT_REQUESTED, groupId = "export-worker")
    @Transactional
    public void processExport(String message) {
        try {
            ExportJob job = objectMapper.readValue(message, ExportJob.class);
            job = repository.findById(job.getId()).orElseThrow();
            job.setStatus("PROCESSING");
            repository.save(job);

            Files.createDirectories(Path.of(storagePath));
            Path file = Path.of(storagePath, job.getId() + ".xlsx");

            WebClient webClient = webClientBuilder.build();
            var jobs = webClient.get()
                    .uri(jobServiceUrl + "/api/jobs?size=1000")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block(Duration.ofSeconds(5));

            try (XSSFWorkbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Jobs");
                Row header = sheet.createRow(0);
                header.createCell(0).setCellValue("Export ID");
                header.createCell(1).setCellValue("User ID");
                header.createCell(2).setCellValue("Generated At");
                Row row = sheet.createRow(1);
                row.createCell(0).setCellValue(job.getId().toString());
                row.createCell(1).setCellValue(job.getUserId().toString());
                row.createCell(2).setCellValue(Instant.now().toString());
                try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
                    workbook.write(fos);
                }
            }

            job.setStatus("COMPLETED");
            job.setFilePath(file.toString());
            job.setCompletedAt(Instant.now());
            repository.save(job);
            kafkaTemplate.send(KafkaTopics.EXPORT_COMPLETED, job.getId().toString(), objectMapper.writeValueAsString(job));
        } catch (Exception ex) {
            log.error("Export failed", ex);
            try {
                ExportJob job = objectMapper.readValue(message, ExportJob.class);
                repository.findById(job.getId()).ifPresent(j -> {
                    j.setStatus("FAILED");
                    j.setErrorMessage(ex.getMessage());
                    j.setCompletedAt(Instant.now());
                    repository.save(j);
                });
                kafkaTemplate.send(KafkaTopics.EXPORT_FAILED, job.getId().toString(), ex.getMessage());
            } catch (Exception ignored) {}
        }
    }
}
