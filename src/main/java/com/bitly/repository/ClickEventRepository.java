package com.bitly.repository;

import com.bitly.model.ClickEvent;
import com.bitly.model.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA repository for ClickEvent entity operations.
 */
@Repository
public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {

    /**
     * Finds click events for a specific URL mapping, sorted by timestamp descending.
     */
    List<ClickEvent> findByUrlMappingOrderByClickedAtDesc(UrlMapping urlMapping);

    /**
     * Counts the total number of click events for a specific URL mapping.
     */
    long countByUrlMapping(UrlMapping urlMapping);
}
