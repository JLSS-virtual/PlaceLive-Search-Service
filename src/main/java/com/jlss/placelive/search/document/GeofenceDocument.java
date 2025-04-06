package com.jlss.placelive.search.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "geofences")
public class GeofenceDocument {

    @Id
    private Long geofenceId;

    private long placeId;

    private double radius;

    private Boolean isActive;

    private Boolean notificationsEnabled;

    private String notificationMessage;

    private int entryCount;

    private int exitCount;

    private double latitude;

    private double longitude;
}
