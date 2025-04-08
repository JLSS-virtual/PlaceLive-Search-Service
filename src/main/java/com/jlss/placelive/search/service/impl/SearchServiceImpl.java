package com.jlss.placelive.search.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {


    // [CHANGED] Replaced repositories and operations with the new ElasticsearchClient
    private final ElasticsearchClient elasticsearchClient;
    private final PlaceRepository placeRepo;
    private final GeofenceRepository geofenceRepo;
    private final TrackerRepository trackerRepo;
    private final UserRepository userRepo;


    @Autowired
    public SearchServiceImpl(ElasticsearchClient elasticsearchClient, ElasticsearchClient elasticsearchClient1, PlaceRepository placeRepo, GeofenceRepository geofenceRepo, TrackerRepository trackerRepo, UserRepository userRepo){
        this.elasticsearchClient = elasticsearchClient1;
        this.placeRepo = placeRepo;
        this.geofenceRepo = geofenceRepo;
        this.trackerRepo = trackerRepo;
        this.userRepo = userRepo;
    }

    // ------------------ Indexing Methods ------------------

    @Override
    public void indexPlace(PlaceDocument placeDocument) {
        try {
            placeRepo.save(placeDocument);
        } catch (Exception e) {
            throw new RuntimeException("Error indexing PlaceDocument", e);
        }
    }

    @Override
    public void indexGeofence(GeofenceDocument geofenceDocument) {
        try {
            geofenceRepo.save(geofenceDocument);
        } catch (Exception e) {
            throw new RuntimeException("Error indexing GeofenceDocument", e);
        }
    }

    @Override
    public void indexUser(UserDocument userDocument) {
        try {
            userRepo.save(userDocument);
        } catch (Exception e) {
            throw new RuntimeException("Error indexing UserDocument", e);
        }
    }

    @Override
    public void indexTracker(TrackerDocument trackerDocument) {
        try {
            trackerRepo.save(trackerDocument);
        } catch (Exception e) {
            throw new RuntimeException("Error indexing TrackerDocument", e);
        }
    }


    @Override
    public void deletePlace(Long id) {
        try {
            placeRepo.deleteById(String.valueOf(id));
        } catch (Exception e) {
            throw new RuntimeException("Error deleting PlaceDocument", e);
        }
    }

    @Override
    public void deleteGeofence(Long id) {
        try {
            geofenceRepo.deleteById(String.valueOf(id));
        } catch (Exception e) {
            throw new RuntimeException("Error deleting GeofenceDocument", e);
        }
    }

    @Override
    public void deleteUser(Long id) {
        try {
            userRepo.deleteById(String.valueOf(id));
        } catch (Exception e) {
            throw new RuntimeException("Error deleting UserDocument", e);
        }
    }

    @Override
    public void deleteTracker(Long id) {
        try {
            trackerRepo.deleteById(String.valueOf(id));
        } catch (Exception e) {
            throw new RuntimeException("Error deleting TrackerDocument", e);
        }
    }


    // ------------------ Search Methods ------------------

    @Override
    public List<Object> search(String query, String filter, Long userId) {
        if ("ENHANCED".equalsIgnoreCase(filter)) {
            return new ArrayList<>(enhancedSocialSearch(query, userId));
        } else {
            return basicSearch(query);
        }
    }

    // Basic search using the new client for multi-index search
    private List<Object> basicSearch(String query) {
        List<Object> results = new ArrayList<>();
        try {
            // [CHANGED] Building a multi-match query for PlaceDocument
            SearchResponse<PlaceDocument> placeResponse = elasticsearchClient.search(s -> s
                    .index("places")
                            .query(q -> q
                                    .multiMatch(mm -> mm
                                            .query(query)
                                            .fields("name^3", "location^1.5")
                                            .fuzziness("AUTO")
                                            .type(TextQueryType.BestFields)
                                            .minimumShouldMatch("70%")
                                    )
                            ),
                                PlaceDocument.class);

            List<PlaceDocument> placeHits = placeResponse.hits().hits()
                    .stream().map(Hit::source).toList();
            results.addAll(placeHits);

            // [CHANGED] Similarly for GeofenceDocument
            SearchResponse<GeofenceDocument> geofenceResponse = elasticsearchClient.search(s -> s
                    .index("geofences")
                    .query(q -> q
                            .multiMatch(mm -> mm
                                    .query(query)
                                    .fields("region^3", "description^1.5")
                                    .fuzziness("AUTO")
                                    .type(TextQueryType.BestFields)
                                    .minimumShouldMatch("75%")
                            )
                    ), GeofenceDocument.class);
            List<GeofenceDocument> geofenceHits = geofenceResponse.hits().hits()
                    .stream().map(Hit::source).toList();
            results.addAll(geofenceHits);

            // [CHANGED] And for UserDocument
            SearchResponse<UserDocument> userResponse = elasticsearchClient.search(s -> s
                    .index("users")
                    .query(q -> q
                            .multiMatch(mm -> mm
                                    .query(query)
                                    .fields("name","followers") // single field here with boost logic can be adjusted
                                    .fuzziness("AUTO")
                                    .type(TextQueryType.BestFields)
                                    .minimumShouldMatch("75%")
                            )
                    ), UserDocument.class);
            List<UserDocument> userHits = userResponse.hits().hits()
                    .stream().map(Hit::source).toList();
            results.addAll(userHits);
        } catch (Exception e) {
            throw new RuntimeException("Error executing basic search", e);
        }
        return results;
    }

    // Enhanced search: find places and enrich them with friends data.
    private List<PlaceWithFriendsDTO> enhancedSocialSearch(String query, Long userId) {
        List<PlaceWithFriendsDTO> enhancedResults = new ArrayList<>();
        try {
            // [CHANGED] Basic search on places using new client
            SearchResponse<PlaceDocument> placeResponse = elasticsearchClient.search(s -> s
                    .index("places")
                    .query(q -> q
                            .multiMatch(mm -> mm
                                    .query(query)
                                    .fields("name^3", "location^1.5")
                                    .fuzziness("AUTO")
                                    .type(TextQueryType.BestFields)
                                    .minimumShouldMatch("70%")
                            )
                    ), PlaceDocument.class);

            List<PlaceDocument> places = placeResponse.hits().hits()
                    .stream().map(Hit::source).toList();

            // For each place, enrich with friend data (logic remains the same)
            for (PlaceDocument place : places) {
                // Retrieve geofences for the current place.
                List<GeofenceDocument> geofences = /* [CHANGED] Replace with appropriate new client call or repository method if needed */
                        getGeofencesByPlaceId(place.getId());
                if (geofences.isEmpty()) {
                    continue;
                }
                List<String> geofenceIds = geofences.stream()
                        .map(GeofenceDocument::getGeofenceId)
                        .collect(Collectors.toList());

                // Retrieve the user's friends list.
                List<UserDocument> friendList = /* [CHANGED] Replace with new client call or external service call if needed */
                        getFriendsByUserId(String.valueOf(userId));
                if (friendList.isEmpty()) {
                    enhancedResults.add(new PlaceWithFriendsDTO(place, Collections.emptyList()));
                    continue;
                }
                List<String> friendIds = friendList.stream()
                        .map(UserDocument::getUserId)
                        .collect(Collectors.toList());

                // Retrieve tracker entries for active friends.
                List<TrackerDocument> trackerDocs = /* [CHANGED] Replace with new client call or repository method if needed */
                        getActiveTrackerEntries(geofenceIds, friendIds);

                List<String> activeFriendIds = trackerDocs.stream()
                        .map(TrackerDocument::getUserId)
                        .distinct()
                        .collect(Collectors.toList());

                List<UserDocument> activeFriends = /* [CHANGED] Replace with new client call or repository method if needed */
                        getUsersByIds(activeFriendIds);

                enhancedResults.add(new PlaceWithFriendsDTO(place, activeFriends));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error executing enhanced social search", e);
        }
        return enhancedResults;
    }

    private List<GeofenceDocument> getGeofencesByPlaceId(String placeId) {
        return geofenceRepo.findByPlaceId(placeId);
    }

    private List<UserDocument> getFriendsByUserId(String userId) {
        // Assuming friend IDs are stored and can be queried
        return userRepo.findFriendsByUserId(userId);
    }

    private List<TrackerDocument> getActiveTrackerEntries(List<String> geofenceIds, List<String> friendIds) {
        return trackerRepo.findByGeofenceIdInAndUserIdInAndIsUserInTrue(geofenceIds, friendIds);
    }

    private List<UserDocument> getUsersByIds(List<String> userIds) {
        return (List<UserDocument>) userRepo.findAllById(userIds);
    }

}
