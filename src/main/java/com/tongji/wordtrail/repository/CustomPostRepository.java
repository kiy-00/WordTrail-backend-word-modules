package com.tongji.wordtrail.repository;

import com.tongji.wordtrail.model.Post;

import java.util.List;

public interface CustomPostRepository {
    List<Post> findRandomPosts(int size);
}
