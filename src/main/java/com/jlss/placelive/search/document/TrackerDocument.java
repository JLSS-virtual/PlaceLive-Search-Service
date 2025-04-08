package com.jlss.placelive.search.document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "tracker")
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrackerDocument {

    /**
     * Tracker specific data
     **/
    @Id
    private String id;

    private String name;

    private boolean isUserIn;

    private Date trackerCreatedAt;

    private Date lastVisitAt;

    private Date lastExitAt;

    private long userVisitCount;

    /**
     * IDs
     **/
    private String geofenceId;

    private String userId;

    private String placeId;

    /**
     * Geofence-related data
     **/
    private Double latitude;

    private Double longitude;

    private Double radius;
}
