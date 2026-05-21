package com.bitly.service;

import com.bitly.dto.CreateUrlRequest;
import com.bitly.dto.UrlResponse;
import com.bitly.exception.*;
import com.bitly.model.ClickEvent;
import com.bitly.model.UrlMapping;
import com.bitly.model.User;
import com.bitly.repository.ClickEventRepository;
import com.bitly.repository.UrlMappingRepository;
import com.bitly.util.Base62Encoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Core service handling URL shortening, resolution, ownership, and click event analytics.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UrlService {

    private final UrlMappingRepository repository;
    private final ClickEventRepository clickEventRepository;
    private final Base62Encoder base62Encoder;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.short-code-length}")
    private int shortCodeLength;

    /**
     * Creates a new shortened URL mapping associated with a specific user.
     * Generates a unique Base62 short code or uses a custom alias if provided.
     * Optional link password can be configured.
     *
     * @param request the URL creation request
     * @param user    the authenticated user owning the URL
     * @return the created URL mapping response
     */
    @Transactional
    public UrlResponse createShortUrl(CreateUrlRequest request, User user) {
        String shortCode;

        if (request.getCustomAlias() != null && !request.getCustomAlias().isBlank()) {
            shortCode = request.getCustomAlias().trim();
            if (repository.existsByShortCode(shortCode)) {
                throw new ShortCodeAlreadyExistsException(shortCode);
            }
        } else {
            shortCode = generateUniqueShortCode();
        }

        String passwordHash = null;
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            passwordHash = passwordEncoder.encode(request.getPassword().trim());
        }

        UrlMapping mapping = UrlMapping.builder()
                .shortCode(shortCode)
                .originalUrl(request.getUrl())
                .expiresAt(request.getExpiresAt())
                .user(user)
                .passwordHash(passwordHash)
                .clickCount(0L)
                .build();

        UrlMapping saved = repository.save(mapping);
        log.info("Created short URL for user '{}': {} -> {}",
                user != null ? user.getUsername() : "anonymous", shortCode, request.getUrl());

        return mapToResponse(saved);
    }

    /**
     * Resolves a short code to the original URL.
     * Checks for expiration and password protection.
     * Records a click event if access is not password protected.
     *
     * @param shortCode the short code to resolve
     * @param referrer  HTTP referrer header value
     * @param userAgent User-Agent header value
     * @param ipAddress client IP address
     * @return the original URL
     */
    @Transactional
    public String resolveUrl(String shortCode, String referrer, String userAgent, String ipAddress) {
        UrlMapping mapping = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));

        if (mapping.isExpired()) {
            throw new UrlExpiredException(shortCode);
        }

        // If URL is password protected, prevent direct redirection without password
        if (mapping.getPasswordHash() != null && !mapping.getPasswordHash().isEmpty()) {
            throw new PasswordRequiredException(shortCode);
        }

        recordClickWithEvent(mapping, referrer, userAgent, ipAddress);

        log.debug("Resolved short code '{}' -> '{}' (clicks: {})",
                shortCode, mapping.getOriginalUrl(), mapping.getClickCount());

        return mapping.getOriginalUrl();
    }

    /**
     * Resolves a password-protected short code by validating the provided password.
     * Records a click event upon successful validation.
     */
    @Transactional
    public String resolveUrlWithPassword(String shortCode, String password, String referrer, String userAgent, String ipAddress) {
        UrlMapping mapping = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));

        if (mapping.isExpired()) {
            throw new UrlExpiredException(shortCode);
        }

        if (mapping.getPasswordHash() != null && !mapping.getPasswordHash().isEmpty()) {
            if (password == null || password.isEmpty()) {
                throw new PasswordRequiredException(shortCode);
            }
            if (!passwordEncoder.matches(password.trim(), mapping.getPasswordHash())) {
                throw new InvalidPasswordException();
            }
        }

        recordClickWithEvent(mapping, referrer, userAgent, ipAddress);

        return mapping.getOriginalUrl();
    }

    /**
     * Retrieves URL mapping details and analytics by short code, ensuring ownership.
     */
    @Transactional(readOnly = true)
    public UrlResponse getUrlStats(String shortCode, User user) {
        UrlMapping mapping = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));

        if (mapping.getUser() != null && (user == null || !mapping.getUser().getId().equals(user.getId()))) {
            throw new AccessDeniedException("You do not have permission to view stats for this URL");
        }

        return mapToResponse(mapping);
    }

    /**
     * Retrieves all URL mappings belonging to the authenticated user.
     */
    @Transactional(readOnly = true)
    public List<UrlResponse> getAllUrls(User user) {
        return repository.findByUser(user, Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Deletes a URL mapping by its short code, verifying ownership.
     */
    @Transactional
    public void deleteUrl(String shortCode, User user) {
        UrlMapping mapping = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));

        if (mapping.getUser() != null && (user == null || !mapping.getUser().getId().equals(user.getId()))) {
            throw new AccessDeniedException("You do not have permission to delete this URL");
        }

        repository.delete(mapping);
        log.info("Deleted short URL: {}", shortCode);
    }

    /**
     * Searches URL mappings belonging to a user by original URL or short code, with sorting.
     */
    @Transactional(readOnly = true)
    public List<UrlResponse> searchUrls(User user, String query, String sortBy, String sortDir) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String property = "clickCount".equalsIgnoreCase(sortBy) ? "clickCount" : "createdAt";
        Sort sort = Sort.by(direction, property);

        List<UrlMapping> mappings;
        if (query == null || query.isBlank()) {
            mappings = repository.findByUser(user, sort);
        } else {
            mappings = repository.searchByUserAndQuery(user, query.trim(), sort);
        }

        return mappings.stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Records a click count increment and tracks a detailed ClickEvent.
     */
    private void recordClickWithEvent(UrlMapping mapping, String referrer, String userAgent, String ipAddress) {
        mapping.recordClick();
        repository.save(mapping);

        ClickEvent clickEvent = ClickEvent.builder()
                .urlMapping(mapping)
                .referrer(referrer)
                .userAgent(userAgent)
                .deviceType(getDeviceType(userAgent))
                .ipAddress(ipAddress)
                .build();
        clickEventRepository.save(clickEvent);
    }

    /**
     * Parses User-Agent string to detect basic device types.
     */
    private String getDeviceType(String userAgent) {
        if (userAgent == null) return "Unknown";
        String ua = userAgent.toLowerCase();
        if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) {
            return "Mobile";
        } else if (ua.contains("tablet") || ua.contains("ipad")) {
            return "Tablet";
        }
        return "Desktop";
    }

    /**
     * Generates a unique short code by retrying on collisions.
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
                .passwordProtected(mapping.getPasswordHash() != null && !mapping.getPasswordHash().isEmpty())
                .build();
    }
}
