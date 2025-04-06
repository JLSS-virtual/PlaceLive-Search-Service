package com.jlss.placelive.search.service.impl;

import com.jlss.placelive.search.document.GeofenceDocument;
import com.jlss.placelive.search.document.PlaceDocument;
import com.jlss.placelive.search.document.TrackerDocument;
import com.jlss.placelive.search.document.UserDocument;
import com.jlss.placelive.search.dto.PlaceWithFriendsDTO;
import com.jlss.placelive.search.repository.GeofenceRepository;
import com.jlss.placelive.search.repository.PlaceRepository;
import com.jlss.placelive.search.repository.TrackerRepository;
import com.jlss.placelive.search.repository.UserRepository;
import com.jlss.placelive.search.service.SearchService;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {

    private final PlaceRepository placeRepository;
    private final GeofenceRepository geofenceRepository;
    private final UserRepository userRepository;
    private final TrackerRepository trackerRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    @Autowired
    public SearchServiceImpl(PlaceRepository placeRepository,
                             GeofenceRepository geofenceRepository,
                             UserRepository userRepository,
                             TrackerRepository trackerRepository,
                             ElasticsearchOperations elasticsearchOperations) {
        this.placeRepository = placeRepository;
        this.geofenceRepository = geofenceRepository;
        this.userRepository = userRepository;
        this.trackerRepository = trackerRepository;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    // Indexing methods remain unchanged ...
    @Override
    public void indexPlace(PlaceDocument placeDocument) {

        placeRepository.save(placeDocument);
    }
    @Override
    public void indexGeofence(GeofenceDocument geofenceDocument) {
        geofenceRepository.save(geofenceDocument);
    }
    @Override
    public void indexUser(UserDocument userDocument) {
        userRepository.save(userDocument);
    }
    @Override
    public void indexTracker(TrackerDocument trackerDocument) {
        trackerRepository.save(trackerDocument);
    }
    @Override
    public void deletePlace(Long id) {
        placeRepository.deleteById(id);
    }
    @Override
    public void deleteGeofence(Long id) {
        geofenceRepository.deleteById(id);
    }
    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    @Override
    public void deleteTracker(Long id) {
        trackerRepository.deleteById(id);
    }

    // Updated search method that branches based on the 'filter' value.
    @Override
    public List<Object> search(String query, String filter, Long userId) {
        if ("ENHANCED".equalsIgnoreCase(filter)) {
            return new ArrayList<>(enhancedSocialSearch(query, userId));
        } else {
            // Perform basic search (existing logic)
            return basicSearch(query);
        }
    }

    // Basic search (unchanged from your current implementation)
    private List<Object> basicSearch(String query) {
        List<Object> results = new ArrayList<>();

        // Searching on PlaceDocument
        NativeSearchQuery placeQuery = new NativeSearchQueryBuilder().withQuery(
                QueryBuilders.multiMatchQuery(query)
                        .field("name", 3.0f)
                        .field("location", 1.5f)
                        .fuzziness(Fuzziness.AUTO)
                        .type(MultiMatchQueryBuilder.Type.BEST_FIELDS)
                        .minimumShouldMatch("70%")
        ).build();

        SearchHits<PlaceDocument> placeHits = elasticsearchOperations.search(placeQuery, PlaceDocument.class);
        results.addAll(
                placeHits.getSearchHits()
                        .stream()
                        .map(SearchHit::getContent)
                        .collect(Collectors.toList())
        );

        // Searching on GeofenceDocument
        NativeSearchQuery geofenceQuery = new NativeSearchQueryBuilder().withQuery(
                QueryBuilders.multiMatchQuery(query)
                        .field("region", 3.0f)
                        .field("description", 1.5f)
                        .fuzziness(Fuzziness.AUTO)
                        .type(MultiMatchQueryBuilder.Type.BEST_FIELDS)
                        .minimumShouldMatch("75%")
        ).build();

        SearchHits<GeofenceDocument> geofenceHits = elasticsearchOperations.search(geofenceQuery, GeofenceDocument.class);
        results.addAll(
                geofenceHits.getSearchHits()
                        .stream()
                        .map(SearchHit::getContent)
                        .collect(Collectors.toList())
        );

        // Searching on UserDocument
        NativeSearchQuery userQuery = new NativeSearchQueryBuilder().withQuery(
                QueryBuilders.multiMatchQuery(query)
                        .field("name", 3.0f)
                        .fuzziness(Fuzziness.AUTO)
                        .type(MultiMatchQueryBuilder.Type.BEST_FIELDS)
                        .minimumShouldMatch("75%")
        ).build();
        SearchHits<UserDocument> userHits = elasticsearchOperations.search(userQuery, UserDocument.class);
        results.addAll(
                userHits.getSearchHits()
                        .stream()
                        .map(SearchHit::getContent)
                        .collect(Collectors.toList())
        );

        return results;
    }

    // Enhanced search: find places and enrich them with friends data.
    private List<PlaceWithFriendsDTO> enhancedSocialSearch(String query, Long userId) {
        // 1. Basic search on places
        NativeSearchQuery placeQuery = new NativeSearchQueryBuilder().withQuery(
                QueryBuilders.multiMatchQuery(query)
                        .field("name", 3.0f)
                        .field("location", 1.5f)
                        .fuzziness(Fuzziness.AUTO)
                        .type(MultiMatchQueryBuilder.Type.BEST_FIELDS)
                        .minimumShouldMatch("70%")
        ).build();
        SearchHits<PlaceDocument> placeHits = elasticsearchOperations.search(placeQuery, PlaceDocument.class);
        List<PlaceDocument> places = placeHits.getSearchHits()
                .stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        // 2. For each place, find associated geofences by place id (assuming PlaceDocument has an 'id' field)
        //    and then, based on those geofences, find tracker entries for friends.
        List<PlaceWithFriendsDTO> enhancedResults = new ArrayList<>();
        for (PlaceDocument place : places) {
            // Retrieve geofences for the current place. (Assuming geofence has a field 'placeId')
            List<GeofenceDocument> geofences = geofenceRepository.findByPlaceId(place.getId());
            if (geofences.isEmpty()) {
                continue;
            }
            // Collect geofence ids
            List<Long> geofenceIds = geofences.stream()
                    .map(GeofenceDocument::getGeofenceId)
                    .collect(Collectors.toList());

            // Retrieve the user's friends list (Assuming userRepository provides this method)
            List<UserDocument> friendList = userRepository.findFriendsByUserId(userId);
            if (friendList.isEmpty()) {
                // If no friends, just add place with an empty friends list.
                enhancedResults.add(new PlaceWithFriendsDTO(place, Collections.emptyList()));
                continue;
            }
            // Get friend ids
            List<Long> friendIds = friendList.stream()
                    .map(UserDocument::getId)
                    .collect(Collectors.toList());

            // 3. Use trackerRepository to find tracker entries where:
            //      - geofenceId is in geofenceIds
            //      - userId is in friendIds
            //      - isUserIn flag is true (assuming TrackerDocument has such a field)
            List<TrackerDocument> trackerDocs = trackerRepository.findByGeofenceIdInAndUserIdInAndIsUserInTrue(geofenceIds, friendIds);

            // 4. From tracker entries, extract friend IDs and retrieve corresponding UserDocuments.
            List<Long> activeFriendIds = trackerDocs.stream()
                    .map(TrackerDocument::getUserId)
                    .distinct()
                    .collect(Collectors.toList());

            List<UserDocument> activeFriends = userRepository.findByIdIn(activeFriendIds);

            // 5. Create the DTO combining the place data and its active friends
            PlaceWithFriendsDTO dto = new PlaceWithFriendsDTO(place, activeFriends);
            enhancedResults.add(dto);
        }

        return enhancedResults;
    }
}
