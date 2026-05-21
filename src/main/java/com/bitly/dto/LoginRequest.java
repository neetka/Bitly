package com.bitly.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for user login.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload to log in")
public class LoginRequest {

    @NotBlank(message = "Username is required")
    @Schema(description = "The registered username", example = "johndoe")
    private String username;

    @NotBlank(message = "Password is required")
    @Schema(description = "The password", example = "mySecurePassword123")
    private String password;
}
