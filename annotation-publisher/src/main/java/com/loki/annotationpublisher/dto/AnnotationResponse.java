package com.loki.annotationpublisher.dto;

public record AnnotationResponse(
        boolean success,
        String message
) {}