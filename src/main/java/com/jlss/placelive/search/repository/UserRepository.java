package com.jlss.placelive.search.repository;

import com.jlss.placelive.search.document.UserDocument;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface UserRepository extends ElasticsearchRepository<UserDocument,String> {
    List<UserDocument> findFriendsByUserId(String userId);
    List<UserDocument> findByUserIdIn(List<String> followers);
}
