package com.bitly.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO returning user info upon authentication.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response payload returning user info upon authentication")
public class AuthResponse {

    @Schema(description = "The authenticated user's username", example = "johndoe")
    private String username;

    @Schema(description = "The authenticated user's email address", example = "johndoe@example.com")
    private String email;

    @Schema(description = "A status or informational message", example = "User registered successfully")
    private String message;
}
