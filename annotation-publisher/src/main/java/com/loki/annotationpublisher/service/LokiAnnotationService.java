package com.loki.annotationpublisher.service;

import com.loki.annotationpublisher.config.LokiConfig;
import com.loki.annotationpublisher.dto.AnnotationRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class LokiAnnotationService {

    private final RestClient lokiRestClient;
    private final LokiConfig lokiConfig;

    public LokiAnnotationService(final RestClient lokiRestClient,
                                 final LokiConfig lokiConfig) {
        this.lokiRestClient = lokiRestClient;
        this.lokiConfig = lokiConfig;
    }

    public void publishAnnotation(final AnnotationRequest request) {
        final String timestampNanos = String.valueOf(Instant.now().toEpochMilli() * 1_000_000);

        final String message = String.format(
                "Grafana annotation published | date=%s | version=%s | comment=%s",
                request.date(),
                request.version(),
                request.comment()
        );

        final Map<String, Object> payload = Map.of(
                "streams", List.of(
                        Map.of(
                                "stream", Map.of(
                                        "app", lokiConfig.appName(),
                                        "env", lokiConfig.environment(),
                                        "type", "grafana-annotation",
                                        "version", request.version()
                                ),
                                "values", List.of(
                                        List.of(timestampNanos, message)
                                )
                        )
                )
        );

        lokiRestClient.post()
                .uri("/loki/api/v1/push")
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .toBodilessEntity();
    }
}