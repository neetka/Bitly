package com.bitly.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for resolving a password-protected short URL.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload to resolve a password-protected URL")
public class ResolvePasswordRequest {

    @Schema(description = "The password to unlock the URL", example = "mySecretPassword123")
    private String password;
}
