package com.tongji.wordtrail.repository;

import com.tongji.wordtrail.model.Post;
import com.tongji.wordtrail.model.Vote;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoteRepository extends MongoRepository<Vote, String> {
    List<Vote> findByUserId(String userId);
    boolean existsByUserId(String userId);
    List<Vote> findByPostIdAndUserId(String postId, String userId);
    List<Vote> findByPostId(String postId);
}
