package com.jlss.placelive.search.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "tracker")
public class TrackerDocument {

    /**
     * Tracker specific data
     **/
    @Id
    private Long id;

    private String name;

    private boolean isUserIn;

    private Date trackerCreatedAt;

    private Date lastVisitAt;

    private Date lastExitAt;

    private long userVisitCount;

    /**
     * IDs
     **/
    private Long geofenceId;

    private Long userId;

    private Long placeId;

    /**
     * Geofence-related data
     **/
    private Double latitude;

    private Double longitude;

    private Double radius;
}
