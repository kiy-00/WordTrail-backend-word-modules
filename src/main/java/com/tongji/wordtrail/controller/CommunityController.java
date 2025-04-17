package com.tongji.wordtrail.controller;

import cn.hutool.core.io.FileUtil;
import com.tongji.wordtrail.dto.*;
import com.tongji.wordtrail.model.Comment;
import com.tongji.wordtrail.model.Favourite;
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
            @RequestParam("username") String username,
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

        Post post = communityService.createPost(username, title, content, imagePaths);
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
    // 获取帖子信息
    @GetMapping("/post/list")
    public ResponseEntity<?> getPostListByPage(@RequestParam(value = "page", defaultValue = "1") int page) {
        List<PostResponse> postList = communityService.getPostsByPage(page);
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("code", 200);
        responseData.put("msg", null);
        responseData.put("data", postList);
        return ResponseEntity.ok().body(responseData);
    }
    // 随机获取10条帖子
    @GetMapping("/post/random")
    public ResponseEntity<?> getRandomPost() {
        List<PostResponse> postResponses = communityService.getRandomPostResponses();
        return ResponseEntity.ok().body(postResponses);
    }
    // 获取帖子总数
    @GetMapping("/post/count")
    public ResponseEntity<?> getPostCount() {
        long data = communityService.getPostCount();
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("code", 200);
        responseData.put("msg", null);
        responseData.put("data", data);
        return ResponseEntity.ok().body(responseData);
    }
    // 返回某用户所有帖子中的第n页帖子
    @GetMapping("/post/user")
    public ResponseEntity<?> getPostUser(@RequestParam("uid") String uid, @RequestParam("page") int page) {
        List<PostResponse> postList = communityService.getPostsByUserPage(uid, page);
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("code", 200);
        responseData.put("msg", null);
        responseData.put("data", postList);
        return ResponseEntity.ok().body(responseData);

    }
    // 返回关键字搜索后的帖子
    @GetMapping("/post/search")
    public ResponseEntity<?> getPostSearch(@RequestParam("keyword") String keyword, @RequestParam("page") int page) {
        List<PostResponse> postList = communityService.getPostSearch(keyword, page);
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("code", 200);
        responseData.put("msg", null);
        responseData.put("data", postList);
        return ResponseEntity.ok().body(responseData);
    }
    // 返回关键字搜索后的帖子
    @GetMapping("/post/search-count")
    public ResponseEntity<?> getPostSearchCount(@RequestParam("keyword") String keyword) {
        int dataCount = communityService.getPostSearchCount(keyword);
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("code", 200);
        responseData.put("msg", null);
        responseData.put("data", dataCount);
        return ResponseEntity.ok().body(responseData);
    }
    // 返回帖子点赞总数
    @GetMapping("/post/vote")
    public ResponseEntity<?> getPostVote(@RequestParam("postId") String postId, @RequestParam("userId") String userId, @RequestParam("upvote") String upvote) {
        int dataCount = communityService.getPostVoteCount(postId, userId, upvote);
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("code", 200);
        responseData.put("msg", null);
        responseData.put("data", dataCount);
        return ResponseEntity.ok().body(responseData);
    }
    // 返回用户是否点赞
    @GetMapping("/post/isVoted")
    public ResponseEntity<?> getPostIsVoted(@RequestParam("postId") String postId, @RequestParam("userId") String userId) {
        int dataCount = communityService.getPostVote(postId, userId);
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("code", 200);
        responseData.put("msg", null);
        responseData.put("data", dataCount);
        return ResponseEntity.ok().body(responseData);
    }
    // 返回总点赞数量
    @GetMapping("/post/voteCount")
    public ResponseEntity<?> getPostVoteCount(@RequestParam("postId") String postId) {
        int dataCount = communityService.getPostVoteCount(postId);
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("code", 200);
        responseData.put("msg", null);
        responseData.put("data", dataCount);
        return ResponseEntity.ok().body(responseData);
    }
    // 收藏帖子
    @PostMapping("/post/favorite")
    public ResponseEntity<?> addFavourite(@RequestParam("postId") String postId, @RequestParam("userId") String userId) {
        communityService.addFavourite(postId, userId);
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("code", 200);
        responseData.put("msg", null);
        responseData.put("data", 200);
        return ResponseEntity.ok().body(responseData);
    }
    // 取消收藏
    @DeleteMapping("/post/deleteFavorite")
    public ResponseEntity<?> deleteFavorite(@RequestParam("postId") String postId, @RequestParam("userId") String userId) {
        Map<String, Object> responseData = new HashMap<>();
        if (communityService.deleteFavourite(postId, userId)) {
            responseData.put("code", 200);
            responseData.put("msg", null);
            responseData.put("data", 200);
            return ResponseEntity.ok().body(responseData);
        }
        else {
            responseData.put("code", 404);
            responseData.put("msg", 404);
            return ResponseEntity.ok().body(responseData);
        }

    }
    // 罗列所有的收藏
    @PostMapping("/post/listFavorite")
    public ResponseEntity<?> getFavoriteList(@RequestParam("postId") String postId, @RequestParam("userId") String userId) {
        List<Favourite> favoriteList = communityService.getFavoriteList(postId, userId);
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("code", 200);
        responseData.put("msg", null);
        responseData.put("data", favoriteList);
        return ResponseEntity.ok().body(responseData);
    }
    // 添加评论
    @PostMapping("/comment/post")
    public ResponseEntity<?> addComment(@RequestBody CommentRequest request) {
        communityService.addComment(request);
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("code", 200);
        responseData.put("msg", null);
        responseData.put("data", "评论成功");
        return ResponseEntity.ok().body(responseData);
    }
    // 删除评论
    @DeleteMapping("/comment/delete")
    public ResponseEntity<?> deleteComment(@RequestParam("commentId") String commentId) {
        communityService.deleteComment(commentId);
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("code", 200);
        responseData.put("msg", null);
        responseData.put("data", "删除成功");
        return ResponseEntity.ok().body(responseData);
    }
    // 查找帖子中的所有评论
    @GetMapping("/comment/list")
    public ResponseEntity<?> getCommentList(@RequestParam("postId") String postId) {
        List<Comment> comments = communityService.getCommentList(postId);
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("code", 200);
        responseData.put("msg", null);
        responseData.put("data", comments);
        return ResponseEntity.ok().body(responseData);
    }
}
