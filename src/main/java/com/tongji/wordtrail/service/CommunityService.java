package com.tongji.wordtrail.service;

import com.tongji.wordtrail.model.Post;
import com.tongji.wordtrail.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CommunityService {
    @Autowired
    private final PostRepository postRepository;

    @Autowired
    public CommunityService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public Post createPost(String title, String content, List<String> filePaths) {
        // 直接存储字符串路径
        Post post = new Post(title, content, filePaths, LocalDateTime.now());
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
}
