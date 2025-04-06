package com.jlss.placelive.search.controller;

import com.jlss.placelive.search.document.GeofenceDocument;
import com.jlss.placelive.search.document.PlaceDocument;
import com.jlss.placelive.search.document.TrackerDocument;
import com.jlss.placelive.search.document.UserDocument;
import com.jlss.placelive.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/api/elasticSearch")
public class SearchController {

    private final SearchService searchService;

    @Autowired
    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    // Endpoints for indexing (invoked by other microservices)

    @PostMapping("/place")
    public ResponseEntity<Void> indexPlace(@RequestBody PlaceDocument placeDocument) {
        searchService.indexPlace(placeDocument);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/place/{id}")
    public ResponseEntity<Void> updatePlace(@PathVariable Long id, @RequestBody PlaceDocument placeDocument) {
        // In this simple example, updating is just re-indexing the document.
        placeDocument.setId(id);
        searchService.indexPlace(placeDocument);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/place/{id}")
    public ResponseEntity<Void> deletePlace(@PathVariable Long id) {
        searchService.deletePlace(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/geofence")
    public ResponseEntity<Void> indexGeofence(@RequestBody GeofenceDocument geofenceDocument) {
        searchService.indexGeofence(geofenceDocument);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/geofence/{id}")
    public ResponseEntity<Void> updateGeofence(@PathVariable Long id, @RequestBody GeofenceDocument geofenceDocument) {
        geofenceDocument.setGeofenceId(id);
        searchService.indexGeofence(geofenceDocument);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/geofence/{id}")
    public ResponseEntity<Void> deleteGeofence(@PathVariable Long id) {
        searchService.deleteGeofence(id);
        return ResponseEntity.ok().build();
    }

    // endpoints for user
    @PostMapping("/user")
    public ResponseEntity<Void> indexUser(@RequestBody UserDocument userDocument) {
        searchService.indexUser(userDocument);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/user/{id}")
    public ResponseEntity<Void> updateUser(@PathVariable Long id, @RequestBody UserDocument userDocument) {
        // In this simple example, updating is just re-indexing the document.
        userDocument.setId(id);
        searchService.indexUser(userDocument);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        searchService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    // endpoints for Tracker

    @PostMapping("/tracker")
    public ResponseEntity<Void> indexTracker(@RequestBody TrackerDocument trackerDocument) {
        searchService.indexTracker(trackerDocument);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/tracker/{id}")
    public ResponseEntity<Void> updateTracker(@PathVariable Long id, @RequestBody TrackerDocument trackerDocument) {
        // In this simple example, updating is just re-indexing the document.
        trackerDocument.setId(id);
        searchService.indexTracker(trackerDocument);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/tracker/{id}")
    public ResponseEntity<Void> deleteTracker(@PathVariable Long id) {
        searchService.deleteTracker(id);
        return ResponseEntity.ok().build();
    }

    // Endpoint for user search queries
    @GetMapping("/user")
    public ResponseEntity<List<Object>> search(
            @RequestParam("q") String query,
            @RequestParam(value = "filter", required = false) String filter,
            @RequestParam(value = "userId", required = false) Long userId) {
        List<Object> results = searchService.search(query, filter, userId);
        return ResponseEntity.ok(results);
    }

}
