package com.bitly.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO containing shortened URL details and analytics.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Response payload containing shortened URL details")
public class UrlResponse {

    @Schema(description = "Database ID of the URL mapping", example = "1")
    private Long id;

    @Schema(description = "The generated short code", example = "aB3dE7f")
    private String shortCode;

    @Schema(description = "The original long URL",
            example = "https://www.example.com/very/long/path/to/resource")
    private String originalUrl;

    @Schema(description = "The complete shortened URL",
            example = "http://localhost:8080/aB3dE7f")
    private String shortUrl;

    @Schema(description = "Number of times this link has been accessed", example = "42")
    private Long clickCount;

    @Schema(description = "Timestamp when the URL was created")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the URL was last accessed")
    private LocalDateTime lastAccessedAt;

    @Schema(description = "Expiration timestamp for the URL, null if never expires")
    private LocalDateTime expiresAt;

    @Schema(description = "Whether access to the shortened URL is protected by a password", example = "true")
    private Boolean passwordProtected;
}
