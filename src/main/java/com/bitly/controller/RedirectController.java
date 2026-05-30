package com.bitly.controller;

import com.bitly.service.UrlService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller responsible for redirecting short URLs to their original destinations.
 * This is the public-facing endpoint that end users interact with.
 * Detects password protection requirements and redirects to a password prompt page.
 */
@RestController
@RequiredArgsConstructor
@Hidden
public class RedirectController {

    private final UrlService urlService;

    /**
     * Redirects the user to the original URL associated with the short code.
     * Extracts browser details (referrer, user-agent, client IP) to record rich analytics click events.
     * Redirects to the password prompt if password-protected.
     */
    @GetMapping("/{shortCode:[a-zA-Z0-9\\-]{1,20}}")
    public ResponseEntity<Void> redirect(
            @PathVariable String shortCode,
            HttpServletRequest servletRequest) {

        String referrer = servletRequest.getHeader(HttpHeaders.REFERER);
        String userAgent = servletRequest.getHeader(HttpHeaders.USER_AGENT);
        String ipAddress = servletRequest.getRemoteAddr();

        // Handle proxy forwarding setups to get real client IP
        String xForwardedFor = servletRequest.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            ipAddress = xForwardedFor.split(",")[0].trim();
        }

        try {
            String originalUrl = urlService.resolveUrl(shortCode, referrer, userAgent, ipAddress);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.LOCATION, originalUrl);
            headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");

            return ResponseEntity.status(HttpStatus.FOUND)
                    .headers(headers)
                    .build();
        } catch (com.bitly.exception.PasswordRequiredException ex) {
            // Redirect browser to password verification page
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.LOCATION, "/password.html?code=" + shortCode);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .headers(headers)
                    .build();
        }
    }
}
