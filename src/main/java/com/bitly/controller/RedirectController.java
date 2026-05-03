package com.bitly.controller;

import com.bitly.service.UrlService;
import io.swagger.v3.oas.annotations.Hidden;
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
 * Uses regex to only match valid Base62 short codes (1-10 alphanumeric chars),
 * preventing conflicts with static resources like index.html, styles.css, etc.
 */
@RestController
@RequiredArgsConstructor
@Hidden
public class RedirectController {

    private final UrlService urlService;

    /**
     * Redirects the user to the original URL associated with the short code.
     * Only matches paths that look like Base62 codes (alphanumeric, 1-10 chars).
     */
    @GetMapping("/{shortCode:[a-zA-Z0-9\\-]{1,10}}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        String originalUrl = urlService.resolveUrl(shortCode);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.LOCATION, originalUrl);
        headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");

        return ResponseEntity.status(HttpStatus.FOUND)
                .headers(headers)
                .build();
    }
}
