package com.jlss.placelive.search.service;

import com.jlss.placelive.search.document.GeofenceDocument;
import com.jlss.placelive.search.document.PlaceDocument;
import com.jlss.placelive.search.document.TrackerDocument;
import com.jlss.placelive.search.document.UserDocument;

import java.util.List;

public interface SearchService {
     void indexPlace(PlaceDocument placeDocument);
     void indexGeofence(GeofenceDocument geofenceDocument);
     void indexUser(UserDocument userDocument);
     void indexTracker(TrackerDocument trackerDocument);
//     void updatePlace(Long id,PlaceDocument placeDocument);
//     void updateGeofence(Long id,GeofenceDocument geofenceDocument);
//     void updateUser(Long id, UserDocument userDocument);
//     void updateTracker(Long id,TrackerDocument trackerDocument);
     void deletePlace(Long id);
     void deleteGeofence(Long id);
     void deleteUser(Long id);
     void deleteTracker(Long id);
     List<Object> search(String query, String filter, Long userId);

}
