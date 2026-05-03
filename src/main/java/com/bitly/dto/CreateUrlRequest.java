package com.bitly.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;

/**
 * Request DTO for creating a shortened URL.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload to create a shortened URL")
public class CreateUrlRequest {

    @NotBlank(message = "URL is required")
    @URL(message = "Must be a valid URL")
    @Size(max = 2048, message = "URL must not exceed 2048 characters")
    @Schema(description = "The original long URL to shorten",
            example = "https://www.example.com/very/long/path/to/resource?param=value")
    private String url;

    @Future(message = "Expiration date must be in the future")
    @Schema(description = "Optional expiration date for the shortened URL",
            example = "2026-12-31T23:59:59",
            nullable = true)
    private LocalDateTime expiresAt;

    @Size(max = 10, message = "Custom alias must not exceed 10 characters")
    @Schema(description = "Optional custom alias for the short code",
            example = "my-link",
            nullable = true)
    private String customAlias;
}
