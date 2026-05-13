package com.loki.annotationpublisher.controller;

import com.loki.annotationpublisher.dto.AnnotationRequest;
import com.loki.annotationpublisher.dto.AnnotationResponse;
import com.loki.annotationpublisher.service.LokiAnnotationService;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/annotations")
public class AnnotationController {

    private static final Logger logger =
            LogManager.getLogger(AnnotationController.class);

    private final LokiAnnotationService lokiAnnotationService;

    public AnnotationController(final LokiAnnotationService lokiAnnotationService) {
        this.lokiAnnotationService = lokiAnnotationService;
    }

    @PostMapping
    public ResponseEntity<AnnotationResponse> publishAnnotation(final @Valid @RequestBody AnnotationRequest request) {
        logger.info("Received annotation publish request. version={}, date={}", request.version(), request.date());

        logger.debug("Incoming annotation request payload={}", request);

        try {
            lokiAnnotationService.publishAnnotation(request);

            logger.info("Annotation request completed successfully. version={}, date={}", request.version(), request.date());

            return ResponseEntity.ok(new AnnotationResponse(true, "Annotation published to Loki"));

        } catch (final Exception e) {
            logger.error("Annotation request failed. version={}, date={}", request.version(), request.date(), e);

            throw e;
        }
    }
}