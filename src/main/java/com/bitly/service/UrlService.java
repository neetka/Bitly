package com.bitly.service;

import com.bitly.dto.CreateUrlRequest;
import com.bitly.dto.UrlResponse;
import com.bitly.exception.ShortCodeAlreadyExistsException;
import com.bitly.exception.UrlExpiredException;
import com.bitly.exception.UrlNotFoundException;
import com.bitly.model.UrlMapping;
import com.bitly.repository.UrlMappingRepository;
import com.bitly.util.Base62Encoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Core service handling URL shortening, resolution, and analytics.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UrlService {

    private final UrlMappingRepository repository;
    private final Base62Encoder base62Encoder;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.short-code-length}")
    private int shortCodeLength;

    /**
     * Creates a new shortened URL mapping.
     * Generates a unique Base62 short code or uses a custom alias if provided.
     *
     * @param request the URL creation request
     * @return the created URL mapping response
     */
    @Transactional
    public UrlResponse createShortUrl(CreateUrlRequest request) {
        String shortCode;

        if (request.getCustomAlias() != null && !request.getCustomAlias().isBlank()) {
            shortCode = request.getCustomAlias().trim();
            if (repository.existsByShortCode(shortCode)) {
                throw new ShortCodeAlreadyExistsException(shortCode);
            }
        } else {
            shortCode = generateUniqueShortCode();
        }

        UrlMapping mapping = UrlMapping.builder()
                .shortCode(shortCode)
                .originalUrl(request.getUrl())
                .expiresAt(request.getExpiresAt())
                .clickCount(0L)
                .build();

        UrlMapping saved = repository.save(mapping);
        log.info("Created short URL: {} -> {}", shortCode, request.getUrl());

        return mapToResponse(saved);
    }

    /**
     * Resolves a short code to the original URL.
     * Records a click and checks for expiration.
     *
     * @param shortCode the short code to resolve
     * @return the original URL
     */
    @Transactional
    public String resolveUrl(String shortCode) {
        UrlMapping mapping = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));

        if (mapping.isExpired()) {
            throw new UrlExpiredException(shortCode);
        }

        mapping.recordClick();
        repository.save(mapping);

        log.debug("Resolved short code '{}' -> '{}' (clicks: {})",
                shortCode, mapping.getOriginalUrl(), mapping.getClickCount());

        return mapping.getOriginalUrl();
    }

    /**
     * Retrieves URL mapping details and analytics by short code.
     *
     * @param shortCode the short code to look up
     * @return the URL mapping response with analytics data
     */
    @Transactional(readOnly = true)
    public UrlResponse getUrlStats(String shortCode) {
        UrlMapping mapping = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));
        return mapToResponse(mapping);
    }

    /**
     * Retrieves all URL mappings, ordered by creation date (newest first).
     *
     * @return list of all URL mapping responses
     */
    @Transactional(readOnly = true)
    public List<UrlResponse> getAllUrls() {
        return repository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Deletes a URL mapping by its short code.
     *
     * @param shortCode the short code to delete
     */
    @Transactional
    public void deleteUrl(String shortCode) {
        UrlMapping mapping = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));
        repository.delete(mapping);
        log.info("Deleted short URL: {}", shortCode);
    }

    /**
     * Generates a unique short code by retrying on collisions.
     * Collision probability is extremely low with Base62^7 space.
     */
    private String generateUniqueShortCode() {
        int maxAttempts = 10;
        for (int i = 0; i < maxAttempts; i++) {
            String code = base62Encoder.generateShortCode(shortCodeLength);
            if (!repository.existsByShortCode(code)) {
                return code;
            }
            log.warn("Short code collision detected on attempt {}, retrying...", i + 1);
        }
        throw new RuntimeException("Failed to generate unique short code after "
                + maxAttempts + " attempts");
    }

    /**
     * Maps a UrlMapping entity to a UrlResponse DTO.
     */
    private UrlResponse mapToResponse(UrlMapping mapping) {
        return UrlResponse.builder()
                .id(mapping.getId())
                .shortCode(mapping.getShortCode())
                .originalUrl(mapping.getOriginalUrl())
                .shortUrl(baseUrl + "/" + mapping.getShortCode())
                .clickCount(mapping.getClickCount())
                .createdAt(mapping.getCreatedAt())
                .lastAccessedAt(mapping.getLastAccessedAt())
                .expiresAt(mapping.getExpiresAt())
                .build();
    }
}
