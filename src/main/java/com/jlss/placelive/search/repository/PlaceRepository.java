package com.jlss.placelive.search.repository;

import com.jlss.placelive.search.document.PlaceDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaceRepository extends ElasticsearchRepository<PlaceDocument, String> {
    // Custom query methods can go here if needed in future
}
