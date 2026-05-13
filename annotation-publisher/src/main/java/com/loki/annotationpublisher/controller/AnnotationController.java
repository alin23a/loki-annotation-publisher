package com.loki.annotationpublisher.controller;

import com.loki.annotationpublisher.dto.AnnotationRequest;
import com.loki.annotationpublisher.dto.AnnotationResponse;
import com.loki.annotationpublisher.service.LokiAnnotationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/annotations")
public class AnnotationController {

    private final LokiAnnotationService lokiAnnotationService;

    public AnnotationController(LokiAnnotationService lokiAnnotationService) {
        this.lokiAnnotationService = lokiAnnotationService;
    }

    @PostMapping
    public ResponseEntity<AnnotationResponse> publishAnnotation(
            @Valid @RequestBody AnnotationRequest request
    ) {
        lokiAnnotationService.publishAnnotation(request);

        return ResponseEntity.ok(
                new AnnotationResponse(true, "Annotation published to Loki")
        );
    }
}