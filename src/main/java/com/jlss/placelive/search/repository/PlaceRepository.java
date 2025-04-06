package com.jlss.placelive.search.repository;

import com.jlss.placelive.search.document.PlaceDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface PlaceRepository extends ElasticsearchRepository<PlaceDocument, Long>{
    // You can add custom query methods if needed
}
