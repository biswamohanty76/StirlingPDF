package com.digitalservice.telstra.pdfgenerator.configs;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "sterling")
@Data
public class StirlingPdfConfiguration {
    private String baseUrl;
    private Map<String, Object> endpoints;
}
