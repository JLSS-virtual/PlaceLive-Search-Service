package com.jlss.placelive.search.document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "geofences")
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeofenceDocument {

    @Id
    private String geofenceId;

    private String  placeId;

    private double radius;

    private Boolean isActive;

    private Boolean notificationsEnabled;

    private String notificationMessage;

    private int entryCount;

    private int exitCount;

    private double latitude;

    private double longitude;
}
