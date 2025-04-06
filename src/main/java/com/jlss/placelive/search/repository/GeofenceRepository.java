package com.jlss.placelive.search.repository;

import com.jlss.placelive.search.document.GeofenceDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface GeofenceRepository extends ElasticsearchRepository<GeofenceDocument, Long> {
    List<GeofenceDocument> findByPlaceId(Long id);
    // Add custom query methods if needed
}
