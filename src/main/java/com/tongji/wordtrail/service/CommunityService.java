package com.tongji.wordtrail.service;

import com.tongji.wordtrail.dto.PostResponse;
import com.tongji.wordtrail.model.Post;
import com.tongji.wordtrail.repository.PostRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CommunityService {
    @Autowired
    private final PostRepository postRepository;

    @Autowired
    public CommunityService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public Post createPost(String username, String title, String content, List<String> filePaths) {
        // 直接存储字符串路径
        long postCountLong = postRepository.count(); // MongoDB 里统计文档总数
        int postCount = (int) postCountLong;
        // 示例：用帖子总数计算页码（比如每页10个）
        int page = postCount / 10 + 1;
        Post post = new Post(title, content, filePaths, LocalDateTime.now(), LocalDateTime.now(), username, null, 0, 0, page);
        return postRepository.save(post);
    }
    public Optional<Post> getPostById(String id) {
        return postRepository.findById(id);
    }
    public boolean deletePostById(String id) {
        if (postRepository.existsById(id)) {
            postRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<PostResponse> getPostsByPage(int page) {
        List<Post> posts = postRepository.findByPage(page);
        return posts.stream().map(post -> {
            PostResponse response = new PostResponse();
            response.setId(post.getId());
            response.setTitle(post.getTitle());
            response.setContent(post.getContent());
            response.setCreatedTime(post.getCreatedAt().toString());
            response.setUpdatedTime(post.getUpdatedTime().toString());
            response.setAuthor(post.getAuthor());
            response.setUserAvatarUrl(post.getUserAvatarUrl());
            response.setCommentCount(post.getCommentCount());
            response.setVoteCount(post.getVoteCount());
            return response;
        }).collect(Collectors.toList());
    }
    // 随机获取10条帖子
    public List<PostResponse> getRandomPostResponses() {
        List<Post> posts = postRepository.findRandomPosts(10);  // 随机10条
        return posts.stream().map(post -> {
            PostResponse response = new PostResponse();
            response.setId(post.getId());
            response.setTitle(post.getTitle());
            response.setContent(post.getContent());
            response.setCreatedTime(post.getCreatedAt().toString());
            response.setUpdatedTime(post.getUpdatedTime().toString());
            response.setAuthor(post.getAuthor());
            response.setUserAvatarUrl(post.getUserAvatarUrl());
            response.setCommentCount(post.getCommentCount());
            response.setVoteCount(post.getVoteCount());
            return response;
        }).collect(Collectors.toList());
    }
    public long getPostCount() {
        return postRepository.count();
    }
}
