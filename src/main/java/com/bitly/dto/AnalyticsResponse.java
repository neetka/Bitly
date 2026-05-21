package com.bitly.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO returning aggregated and individual analytics for a short URL.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response payload with full aggregated click analytics and history for a shortened URL")
public class AnalyticsResponse {

    @Schema(description = "The short code", example = "aB3dE7f")
    private String shortCode;

    @Schema(description = "The complete short URL", example = "http://localhost:8080/aB3dE7f")
    private String shortUrl;

    @Schema(description = "The original destination URL", example = "https://example.com")
    private String originalUrl;

    @Schema(description = "Total count of clicks recorded", example = "105")
    private Long totalClicks;

    @Schema(description = "List of recent individual click events")
    private List<ClickEventResponse> recentClicks;
}
