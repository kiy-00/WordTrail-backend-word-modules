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
            // 检查用户是否存在，将用户不存在的情况作为404而不是500
            if (!friendService.checkUserExists(senderId)) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "发送者用户不存在");
                error.put("code", "USER_NOT_FOUND");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            if (!friendService.checkUserExists(receiverId)) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "接收者用户不存在");
                error.put("code", "USER_NOT_FOUND");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            // 处理其他可预见的错误情况
            if (senderId.equals(receiverId)) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "不能添加自己为好友");
                error.put("code", "INVALID_REQUEST");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            if (friendService.checkAlreadyFriends(senderId, receiverId)) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "已经是好友关系");
                error.put("code", "ALREADY_FRIENDS");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            if (friendService.checkPendingRequest(senderId, receiverId)) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "已经发送过好友请求，等待对方处理中");
                error.put("code", "REQUEST_PENDING");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            // 处理正常流程
            FriendRequest request = friendService.sendFriendRequest(senderId, receiverId, message);
            return ResponseEntity.ok(request);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "发送好友请求失败: " + e.getMessage());
            error.put("code", "SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 获取收到的好友请求列表
     */
    @GetMapping("/requests/received")
    public ResponseEntity<?> getReceivedFriendRequests(@RequestParam String userId) {
        try {
            // 检查用户是否存在
            if (!friendService.checkUserExists(userId)) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "用户不存在");
                error.put("code", "USER_NOT_FOUND");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            List<Map<String, Object>> requests = friendService.getReceivedFriendRequests(userId);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "获取好友请求失败: " + e.getMessage());
            error.put("code", "SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 获取发送的好友请求列表
     */
    @GetMapping("/requests/sent")
    public ResponseEntity<?> getSentFriendRequests(@RequestParam String userId) {
        try {
            // 检查用户是否存在
            if (!friendService.checkUserExists(userId)) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "用户不存在");
                error.put("code", "USER_NOT_FOUND");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            List<Map<String, Object>> requests = friendService.getSentFriendRequests(userId);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "获取好友请求失败: " + e.getMessage());
            error.put("code", "SERVER_ERROR");
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
            // 检查用户是否存在
            if (!friendService.checkUserExists(userId)) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "用户不存在");
                error.put("code", "USER_NOT_FOUND");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            // 检查请求是否存在
            if (!friendService.checkRequestExists(requestId)) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "好友请求不存在");
                error.put("code", "REQUEST_NOT_FOUND");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            // 检查请求是否发给当前用户
            if (!friendService.isRequestForUser(requestId, userId)) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "无权处理此请求");
                error.put("code", "UNAUTHORIZED");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            // 检查请求状态
            if (!friendService.isPendingRequest(requestId)) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "该请求已被处理");
                error.put("code", "INVALID_STATE");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            Map<String, Object> result = friendService.acceptFriendRequest(requestId, userId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "接受好友请求失败: " + e.getMessage());
            error.put("code", "SERVER_ERROR");
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
            // 检查用户是否存在
            if (!friendService.checkUserExists(userId)) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "用户不存在");
                error.put("code", "USER_NOT_FOUND");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            // 检查请求是否存在
            if (!friendService.checkRequestExists(requestId)) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "好友请求不存在");
                error.put("code", "REQUEST_NOT_FOUND");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            // 检查请求是否发给当前用户
            if (!friendService.isRequestForUser(requestId, userId)) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "无权处理此请求");
                error.put("code", "UNAUTHORIZED");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            // 检查请求状态
            if (!friendService.isPendingRequest(requestId)) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "该请求已被处理");
                error.put("code", "INVALID_STATE");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            FriendRequest request = friendService.rejectFriendRequest(requestId, userId);
            return ResponseEntity.ok(request);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "拒绝好友请求失败: " + e.getMessage());
            error.put("code", "SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 获取好友列表
     */
    @GetMapping("/list")
    public ResponseEntity<?> getFriendList(@RequestParam String userId) {
        try {
            // 检查用户是否存在
            if (!friendService.checkUserExists(userId)) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "用户不存在");
                error.put("code", "USER_NOT_FOUND");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            List<Map<String, Object>> friends = friendService.getUserFriends(userId);
            return ResponseEntity.ok(friends);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "获取好友列表失败: " + e.getMessage());
            error.put("code", "SERVER_ERROR");
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
            // 检查用户是否存在
            if (!friendService.checkUserExists(userId)) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "用户不存在");
                error.put("code", "USER_NOT_FOUND");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            // 检查好友是否存在
            if (!friendService.checkUserExists(friendId)) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "好友用户不存在");
                error.put("code", "USER_NOT_FOUND");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            // 检查是否为好友关系
            if (!friendService.checkAlreadyFriends(userId, friendId)) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "该用户不是你的好友");
                error.put("code", "NOT_FRIEND");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            boolean success = friendService.deleteFriend(userId, friendId);
            Map<String, Boolean> response = new HashMap<>();
            response.put("success", success);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "删除好友失败: " + e.getMessage());
            error.put("code", "SERVER_ERROR");
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
            // 检查用户是否存在
            if (!friendService.checkUserExists(userId)) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "用户不存在");
                error.put("code", "USER_NOT_FOUND");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            // 检查好友是否存在
            if (!friendService.checkUserExists(friendId)) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "好友用户不存在");
                error.put("code", "USER_NOT_FOUND");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            // 检查是否为好友关系
            if (!friendService.checkAlreadyFriends(userId, friendId)) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "该用户不是你的好友");
                error.put("code", "NOT_FRIEND");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            UserFriend friend = friendService.setFriendNickname(userId, friendId, nickname);
            return ResponseEntity.ok(friend);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "设置好友昵称失败: " + e.getMessage());
            error.put("code", "SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}