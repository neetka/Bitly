package com.bitly.repository;

import com.bitly.model.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * JPA repository for URL mapping CRUD operations.
 */
@Repository
public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {

    /**
     * Finds a URL mapping by its short code.
     */
    Optional<UrlMapping> findByShortCode(String shortCode);

    /**
     * Checks if a short code already exists.
     */
    boolean existsByShortCode(String shortCode);

    /**
     * Finds all expired URL mappings for cleanup.
     */
    List<UrlMapping> findByExpiresAtBeforeAndExpiresAtIsNotNull(LocalDateTime now);

    /**
     * Finds all URL mappings ordered by creation date (newest first).
     */
    List<UrlMapping> findAllByOrderByCreatedAtDesc();
}
