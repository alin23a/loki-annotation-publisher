package com.loki.annotationpublisher.service;

import com.loki.annotationpublisher.config.LokiConfig;
import com.loki.annotationpublisher.dto.AnnotationRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LokiAnnotationService {

    private static final Logger logger = LogManager.getLogger(LokiAnnotationService.class);

    private final RestTemplate lokiRestTemplate;
    private final LokiConfig lokiConfig;

    public LokiAnnotationService(final RestTemplate lokiRestTemplate,
                                 final LokiConfig lokiConfig) {
        this.lokiRestTemplate = lokiRestTemplate;
        this.lokiConfig = lokiConfig;
    }

    public void publishAnnotation(final AnnotationRequest request) {
        logger.info("Publishing annotation to Loki. version={}, date={}", request.getVersion(), request.getDate());

        logger.debug("Raw annotation request={}", request);

        if (request.getComment().length() > 500) {
            logger.warn("Annotation comment is unusually long. length={}", request.getComment().length());
        }

        final String timestampNanos = String.valueOf(Instant.now().toEpochMilli() * 1_000_000L);

        final String message = String.format(
                "Grafana annotation published | date=%s | version=%s | comment=%s",
                request.getDate(), request.getVersion(), request.getComment());

        final Map<String, Object> stream = new HashMap<String, Object>();
        stream.put("app", lokiConfig.getAppName());
        stream.put("env", lokiConfig.getEnvironment());
        stream.put("type", "grafana-annotation");
        stream.put("version", request.getVersion());

        final List<List<String>> values = new ArrayList<List<String>>();
        values.add(Arrays.asList(timestampNanos, message));

        final Map<String, Object> streamEntry = new HashMap<String, Object>();
        streamEntry.put("stream", stream);
        streamEntry.put("values", values);

        final Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("streams", Collections.singletonList(streamEntry));

        logger.debug("Generated Loki timestampNanos={}", timestampNanos);
        logger.debug("Generated Loki payload={}", payload);

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        final HttpEntity<Map<String, Object>> entity = new HttpEntity<Map<String, Object>>(payload, headers);

        try {
            logger.info("Sending annotation payload to Loki at {}", lokiConfig.getUrl());

            lokiRestTemplate.postForEntity(
                    lokiConfig.getUrl() + "/loki/api/v1/push",
                    entity,
                    Void.class);

            logger.info("Successfully published annotation to Loki. version={}, date={}", request.getVersion(), request.getDate());

        } catch (final Exception e) {
            logger.error("Failed to publish annotation to Loki. version={}, date={}, lokiUrl={}",
                    request.getVersion(), request.getDate(), lokiConfig.getUrl(), e);

            throw e;
        }
    }
}
