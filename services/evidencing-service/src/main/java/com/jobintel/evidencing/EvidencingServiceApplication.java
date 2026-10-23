package com.jobintel.evidencing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = {"com.jobintel.evidencing", "com.jobintel.common"})
@EnableAsync
public class EvidencingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EvidencingServiceApplication.class, args);
    }
}
