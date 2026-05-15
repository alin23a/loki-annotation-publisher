package com.loki.annotationpublisher.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "loki")
public class LokiConfig {

    private String url;
    private String appName;
    private String environment;
}