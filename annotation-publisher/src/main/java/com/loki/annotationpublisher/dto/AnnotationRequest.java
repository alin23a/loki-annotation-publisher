package com.loki.annotationpublisher.dto;

import jakarta.validation.constraints.NotBlank;

public record AnnotationRequest(
        @NotBlank
        String date,

        @NotBlank
        String version,

        @NotBlank
        String comment
) {}