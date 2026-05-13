package com.loki.annotationpublisher.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "loki")
public record LokiConfig(
        String url,
        String appName,
        String environment
) {
}