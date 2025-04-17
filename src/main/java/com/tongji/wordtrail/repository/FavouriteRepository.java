package com.tongji.wordtrail.repository;

import com.tongji.wordtrail.model.Favourite;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FavouriteRepository extends MongoRepository<Favourite, String> {
    boolean existsByPostIdAndUserId(String postId, String userId);
    void deleteByPostIdAndUserId(String postId, String userId);
    List<Favourite> findByPostIdAndUserId(String postId, String userId);
}
