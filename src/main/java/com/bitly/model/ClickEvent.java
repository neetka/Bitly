package com.bitly.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * JPA entity representing an individual click event for a shortened URL.
 * Stores metadata like timestamp, referrer, user agent, device type, and IP.
 */
@Entity
@Table(name = "click_events", indexes = {
        @Index(name = "idx_url_mapping_id", columnList = "url_mapping_id"),
        @Index(name = "idx_clicked_at", columnList = "clickedAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClickEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "url_mapping_id", nullable = false)
    private UrlMapping urlMapping;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime clickedAt;

    @Column(length = 1024)
    private String referrer;

    @Column(length = 1024)
    private String userAgent;

    @Column(length = 50)
    private String deviceType;

    @Column(length = 45) // IPv6 addresses are up to 45 characters
    private String ipAddress;
}
