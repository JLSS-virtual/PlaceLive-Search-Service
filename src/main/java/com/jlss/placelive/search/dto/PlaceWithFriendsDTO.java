package com.jlss.placelive.search.dto;


import com.jlss.placelive.search.document.PlaceDocument;
import com.jlss.placelive.search.document.UserDocument;
import lombok.*;

import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PlaceWithFriendsDTO {
    private PlaceDocument place;
    private List<UserDocument> friendsInPlace;


}

