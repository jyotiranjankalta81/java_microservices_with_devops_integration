package com.jobintel.evidencing.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI evidencingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("School Evidencing Module API")
                        .description("REST API for Instagram-style school activity evidencing feed, asynchronous media variant processing, and role/scope authorization.")
                        .version("1.0.0"));
    }
}
