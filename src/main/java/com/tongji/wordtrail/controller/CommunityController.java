package com.tongji.wordtrail.controller;

import cn.hutool.core.io.FileUtil;
import com.tongji.wordtrail.dto.AdminWordbooksResponse;
import com.tongji.wordtrail.dto.DeletePostRequest;
import com.tongji.wordtrail.dto.PostRequest;
import com.tongji.wordtrail.model.Post;
import com.tongji.wordtrail.service.CommunityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/forum")
public class CommunityController {
    private static Logger logger = LoggerFactory.getLogger(CommunityController.class);
    private final CommunityService communityService;
    @Autowired
    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }
    private static final String ABSOLUTE_FILE_PATH = "D:\\program3\\Documents\\upload\\images\\";
    // 发布新帖子
    @PostMapping("/post/new")
    public ResponseEntity<?> createNewPost(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        List<String> imagePaths = new ArrayList<>();
        if (files != null) {
            for (MultipartFile file : files) {
                String originName = file.getOriginalFilename();
                String fileName = FileUtil.mainName(originName) + System.currentTimeMillis() + "." + FileUtil.extName(originName);
                String storageName = ABSOLUTE_FILE_PATH  + fileName;
                File dest = new File(storageName);
                if (!dest.getParentFile().exists()) {
                    dest.getParentFile().mkdirs();
                }
                try {
                    file.transferTo(dest);
                    imagePaths.add(storageName); // ✅ 只保存路径
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // 处理标题和内容
        System.out.println("Title: " + title);
        System.out.println("Content: " + content);

        Post post = communityService.createPost(title, content, imagePaths);
        return ResponseEntity.ok().body("{}");
    }


    // 获取帖子详情
    @PostMapping("/post/get")
    public ResponseEntity<?> getPost(@RequestBody Map<String, String> requestData) {
        String id = requestData.get("id");
        if (id == null || id.isEmpty()) {
            return ResponseEntity.badRequest().body("Post ID is required");
        }

        Optional<Post> post = communityService.getPostById(id);

        return post.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    // 删除帖子
    @DeleteMapping("/post/delete")
    public ResponseEntity<?> deletePost(@RequestBody(required = true) DeletePostRequest deletePostRequest) {
        boolean deleted = communityService.deletePostById(deletePostRequest.getId());

        if (deleted) {
            return ResponseEntity.ok().body("{}"); // 返回空 JSON
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
