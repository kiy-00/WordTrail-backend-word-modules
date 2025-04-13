package com.tongji.wordtrail.repository;

import com.tongji.wordtrail.model.Post;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends MongoRepository<Post, String>, CustomPostRepository {
    List<Post> findByPage(int Page);
}

