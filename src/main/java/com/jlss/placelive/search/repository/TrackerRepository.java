package com.jlss.placelive.search.repository;

import com.jlss.placelive.search.document.TrackerDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrackerRepository extends ElasticsearchRepository<TrackerDocument, String> {

    List<TrackerDocument> findByGeofenceIdInAndUserIdInAndIsUserInTrue(
            List<String> geofenceIds, List<String> followers
    );
}
