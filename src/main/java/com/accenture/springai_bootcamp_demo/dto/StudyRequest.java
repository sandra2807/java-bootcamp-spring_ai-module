package com.accenture.springai_bootcamp_demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StudyRequest(
        @NotBlank(message = "topic must not be blank")
        @Size(max = 1000, message = "topic must not exceed 1000 characters")
        String topic) {
}
