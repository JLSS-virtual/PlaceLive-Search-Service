package com.jlss.placelive.search.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.List;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "users")
public class UserDocument {
    @Id
    private Long id;
    private String name;
    private String bio;
    private boolean isLoggedIn;
    private String email;
    private String mobileNumber;
    private String profileImageUrl;

    // Flattened region
    private String country;
    private String state;
    private String city;
    private String street;

    private List<Long> followers;
    private List<Long> following;
    private List<Long> closeFriends;

    private Date accountCreatedAt;
    private Date lastLoginAt;
}
