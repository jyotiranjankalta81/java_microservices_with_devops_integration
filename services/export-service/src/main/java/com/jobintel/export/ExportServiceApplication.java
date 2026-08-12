package com.jobintel.export;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
@ComponentScan(basePackages = {"com.jobintel.export", "com.jobintel.common"})
public class ExportServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExportServiceApplication.class, args);
    }
}
