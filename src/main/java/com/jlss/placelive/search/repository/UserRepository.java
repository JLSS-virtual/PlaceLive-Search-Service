package com.jlss.placelive.search.repository;

import com.jlss.placelive.search.document.UserDocument;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface UserRepository extends ElasticsearchRepository<UserDocument,Long> {
    List<UserDocument> findFriendsByUserId(Long userId);

    List<UserDocument> findByIdIn(List<Long> activeFriendIds);
}
