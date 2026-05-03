package com.bitly.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standardized error response DTO for consistent API error handling.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standardized error response")
public class ErrorResponse {

    @Schema(description = "HTTP status code", example = "404")
    private int status;

    @Schema(description = "Error type", example = "Not Found")
    private String error;

    @Schema(description = "Human-readable error message",
            example = "URL with short code 'abc123' not found")
    private String message;

    @Schema(description = "Request path that caused the error", example = "/api/urls/abc123")
    private String path;

    @Schema(description = "Timestamp of the error occurrence")
    private LocalDateTime timestamp;

    @Schema(description = "Validation error details, keyed by field name")
    private Map<String, String> validationErrors;
}
