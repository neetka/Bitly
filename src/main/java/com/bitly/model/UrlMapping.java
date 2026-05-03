package com.bitly.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * JPA entity representing a URL mapping.
 * Stores the association between a short code and the original URL,
 * along with analytics metadata (click count, last accessed, expiration).
 */
@Entity
@Table(name = "url_mappings", indexes = {
        @Index(name = "idx_short_code", columnList = "shortCode", unique = true),
        @Index(name = "idx_expires_at", columnList = "expiresAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UrlMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String shortCode;

    @Column(nullable = false, length = 2048)
    private String originalUrl;

    @Column(nullable = false)
    @Builder.Default
    private Long clickCount = 0L;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime lastAccessedAt;

    private LocalDateTime expiresAt;

    /**
     * Increments the click count and updates the last accessed timestamp.
     */
    public void recordClick() {
        this.clickCount++;
        this.lastAccessedAt = LocalDateTime.now();
    }

    /**
     * Returns true if this URL mapping has expired.
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
}
