package com.jlss.placelive.search.repository;

import com.jlss.placelive.search.document.TrackerDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface TrackerRepository extends ElasticsearchRepository<TrackerDocument,Long> {
    List<TrackerDocument> findByGeofenceIdInAndUserIdInAndIsUserInTrue(List<Long> geofenceIds, List<Long> friendIds);
}
