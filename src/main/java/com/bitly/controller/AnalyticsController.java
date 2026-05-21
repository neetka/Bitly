package com.bitly.controller;

import com.bitly.dto.AnalyticsResponse;
import com.bitly.dto.ClickEventResponse;
import com.bitly.exception.AccessDeniedException;
import com.bitly.exception.UrlNotFoundException;
import com.bitly.model.ClickEvent;
import com.bitly.model.UrlMapping;
import com.bitly.model.User;
import com.bitly.repository.ClickEventRepository;
import com.bitly.repository.UrlMappingRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller providing deep click metrics and event logs for a shortened URL.
 */
@RestController
@RequestMapping("/api/urls")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Endpoints for link click analytics and history")
public class AnalyticsController {

    private final UrlMappingRepository urlMappingRepository;
    private final ClickEventRepository clickEventRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    @GetMapping("/{shortCode}/analytics")
    @Operation(summary = "Get detailed click analytics for a short code")
    public ResponseEntity<AnalyticsResponse> getUrlAnalytics(
            @PathVariable String shortCode,
            @AuthenticationPrincipal User user) {

        UrlMapping mapping = urlMappingRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));

        // Check ownership
        if (mapping.getUser() != null && (user == null || !mapping.getUser().getId().equals(user.getId()))) {
            throw new AccessDeniedException("You do not have permission to view analytics for this URL");
        }

        List<ClickEvent> clickEvents = clickEventRepository.findByUrlMappingOrderByClickedAtDesc(mapping);

        List<ClickEventResponse> clickEventResponses = clickEvents.stream()
                .map(event -> ClickEventResponse.builder()
                        .clickedAt(event.getClickedAt())
                        .referrer(event.getReferrer() != null && !event.getReferrer().isBlank() ? event.getReferrer() : "Direct / None")
                        .userAgent(event.getUserAgent())
                        .deviceType(event.getDeviceType())
                        .ipAddress(event.getIpAddress())
                        .build())
                .toList();

        return ResponseEntity.ok(
                AnalyticsResponse.builder()
                        .shortCode(mapping.getShortCode())
                        .shortUrl(baseUrl + "/" + mapping.getShortCode())
                        .originalUrl(mapping.getOriginalUrl())
                        .totalClicks(mapping.getClickCount())
                        .recentClicks(clickEventResponses)
                        .build()
        );
    }
}
