package com.tongji.wordtrail.controller;

import com.tongji.wordtrail.dto.AdminWordbooksResponse;
import com.tongji.wordtrail.dto.DeletePostRequest;
import com.tongji.wordtrail.dto.PostRequest;
import com.tongji.wordtrail.model.Post;
import com.tongji.wordtrail.service.CommunityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/forum")
public class CommunityController {
    private static Logger logger = LoggerFactory.getLogger(CommunityController.class);
    private final CommunityService communityService;
    @Autowired
    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }
    // 发布新帖子
    @PostMapping("/post/new")
    public ResponseEntity<Post> createPost(@RequestBody PostRequest postRequest) {

        Post post = communityService.createPost(postRequest.getTitle(), postRequest.getContent(), postRequest.getFiles());
        return ResponseEntity.ok(post);
    }
    // 获取帖子详情
    @GetMapping("/post/get")
    public ResponseEntity<?> getPost(@RequestParam(value = "id", required = false) String id) {
        if (id == null || id.isEmpty()) {
            return ResponseEntity.badRequest().body("Post ID is required");
        }

        Optional<Post> post = communityService.getPostById(id);
        return post.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    // 使用 删除帖子
    @PostMapping("/post/delete")
    public ResponseEntity<?> deletePost(@RequestBody(required = true) DeletePostRequest deletePostRequest) {
        boolean deleted = communityService.deletePostById(deletePostRequest.getId());

        if (deleted) {
            return ResponseEntity.ok().body("{}"); // 返回空 JSON
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
