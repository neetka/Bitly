package com.bitly.controller;

import com.bitly.dto.AuthResponse;
import com.bitly.dto.LoginRequest;
import com.bitly.dto.SignupRequest;
import com.bitly.model.User;
import com.bitly.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

/**
 * Controller handling user registration, programmatic login, and session information.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user registration, login, profile check")
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/signup")
    @Operation(summary = "Register a new user account")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        User user = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                AuthResponse.builder()
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .message("Registration successful. You can now log in.")
                        .build()
        );
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user credentials and establish session")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest) {

        try {
            User user = (User) userService.loadUserByUsername(request.getUsername());

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        AuthResponse.builder().message("Invalid username or password").build()
                );
            }

            // Authenticate user programmatically
            Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Store security context in session for persistence across requests
            HttpSession session = servletRequest.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

            return ResponseEntity.ok(
                    AuthResponse.builder()
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .message("Login successful")
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    AuthResponse.builder().message("Invalid username or password").build()
            );
        }
    }

    @GetMapping("/me")
    @Operation(summary = "Get currently authenticated user details")
    public ResponseEntity<AuthResponse> me(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(
                AuthResponse.builder()
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .build()
        );
    }
}
