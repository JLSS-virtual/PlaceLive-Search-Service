package com.jlss.placelive.search.document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.xml.transform.sax.SAXResult;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "places")
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlaceDocument {

    @Id
    private String id;

    private String name;

    private String description;

    private String country;
    private String state;
    private String city;
    private String Street;

    private Date createdAt;

    private Date updatedAt;

    private String type;

    private List<String> tags; // Stored as list for filtering/searching

    private String ownerId;
}
