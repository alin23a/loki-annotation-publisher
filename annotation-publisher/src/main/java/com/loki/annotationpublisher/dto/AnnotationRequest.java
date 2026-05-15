package com.loki.annotationpublisher.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AnnotationRequest {

    @NotBlank
    private String date;

    @NotBlank
    private String version;

    @NotBlank
    private String comment;
}
