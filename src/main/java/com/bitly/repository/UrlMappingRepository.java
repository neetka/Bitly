package com.bitly.repository;

import com.bitly.model.UrlMapping;
import com.bitly.model.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * Finds all URL mappings belonging to a specific user, sorted dynamically.
     */
    List<UrlMapping> findByUser(User user, Sort sort);

    /**
     * Searches for URL mappings belonging to a specific user by original URL or short code, with dynamic sorting.
     */
    @Query("SELECT u FROM UrlMapping u WHERE u.user = :user AND " +
           "(LOWER(u.originalUrl) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.shortCode) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<UrlMapping> searchByUserAndQuery(@Param("user") User user, @Param("query") String query, Sort sort);
}
