package com.tongji.wordtrail.controller;

import com.tongji.wordtrail.dto.FriendRequest;
import com.tongji.wordtrail.model.UserFriend;
import com.tongji.wordtrail.service.FriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/friends")
public class FriendController {

    private final FriendService friendService;

    @Autowired
    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    /**
     * 发送好友请求
     */
    @PostMapping("/request")
    public ResponseEntity<?> sendFriendRequest(
            @RequestParam String senderId,
            @RequestParam String receiverId,
            @RequestParam(required = false) String message) {
        try {
            FriendRequest request = friendService.sendFriendRequest(senderId, receiverId, message);
            return ResponseEntity.ok(request);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "发送好友请求失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 获取收到的好友请求列表
     */
    @GetMapping("/requests/received")
    public ResponseEntity<?> getReceivedFriendRequests(@RequestParam String userId) {
        try {
            List<Map<String, Object>> requests = friendService.getReceivedFriendRequests(userId);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "获取好友请求失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 获取发送的好友请求列表
     */
    @GetMapping("/requests/sent")
    public ResponseEntity<?> getSentFriendRequests(@RequestParam String userId) {
        try {
            List<Map<String, Object>> requests = friendService.getSentFriendRequests(userId);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "获取好友请求失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 接受好友请求
     */
    @PostMapping("/request/accept")
    public ResponseEntity<?> acceptFriendRequest(
            @RequestParam Long requestId,
            @RequestParam String userId) {
        try {
            Map<String, Object> result = friendService.acceptFriendRequest(requestId, userId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "接受好友请求失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 拒绝好友请求
     */
    @PostMapping("/request/reject")
    public ResponseEntity<?> rejectFriendRequest(
            @RequestParam Long requestId,
            @RequestParam String userId) {
        try {
            FriendRequest request = friendService.rejectFriendRequest(requestId, userId);
            return ResponseEntity.ok(request);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "拒绝好友请求失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 获取好友列表
     */
    @GetMapping("/list")
    public ResponseEntity<?> getFriendList(@RequestParam String userId) {
        try {
            List<Map<String, Object>> friends = friendService.getUserFriends(userId);
            return ResponseEntity.ok(friends);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "获取好友列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 删除好友
     */
    @DeleteMapping("/{friendId}")
    public ResponseEntity<?> deleteFriend(
            @RequestParam String userId,
            @PathVariable String friendId) {
        try {
            boolean success = friendService.deleteFriend(userId, friendId);
            Map<String, Boolean> response = new HashMap<>();
            response.put("success", success);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "删除好友失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 设置好友昵称
     */
    @PutMapping("/nickname")
    public ResponseEntity<?> setFriendNickname(
            @RequestParam String userId,
            @RequestParam String friendId,
            @RequestParam String nickname) {
        try {
            UserFriend friend = friendService.setFriendNickname(userId, friendId, nickname);
            return ResponseEntity.ok(friend);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "设置好友昵称失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}