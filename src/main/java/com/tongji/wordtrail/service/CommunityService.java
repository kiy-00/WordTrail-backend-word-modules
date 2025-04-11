package com.tongji.wordtrail.service;

import com.tongji.wordtrail.dto.PostResponse;
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
    /*
    public List<PostResponse> getPostsByPage(int page) {
        int pageSize = 10;
        int offset = (page - 1) * pageSize;
        return postMapper.getPostList(offset, pageSize);
    }*/

}
