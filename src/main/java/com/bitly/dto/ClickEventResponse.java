package com.bitly.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for returning individual click analytics details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response payload with details of an individual click event")
public class ClickEventResponse {

    @Schema(description = "Timestamp of the click")
    private LocalDateTime clickedAt;

    @Schema(description = "The HTTP referrer of the click", example = "https://t.co/")
    private String referrer;

    @Schema(description = "The browser user agent", example = "Mozilla/5.0 ...")
    private String userAgent;

    @Schema(description = "The device type classified from the user agent", example = "Mobile")
    private String deviceType;

    @Schema(description = "The IP address of the client (masked/partial for privacy)", example = "192.168.1.1")
    private String ipAddress;
}
