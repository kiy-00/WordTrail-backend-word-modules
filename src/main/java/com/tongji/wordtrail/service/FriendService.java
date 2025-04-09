package com.tongji.wordtrail.service;

import com.tongji.wordtrail.dto.FriendRequest;
import com.tongji.wordtrail.model.User;
import com.tongji.wordtrail.model.UserFriend;
import com.tongji.wordtrail.repository.FriendRequestRepository;
import com.tongji.wordtrail.repository.UserFriendRepository;
import com.tongji.wordtrail.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FriendService {

    private final UserFriendRepository friendRepository;
    private final FriendRequestRepository requestRepository;
    private final UserRepository userRepository;

    @Autowired
    public FriendService(
            UserFriendRepository friendRepository,
            FriendRequestRepository requestRepository,
            UserRepository userRepository) {
        this.friendRepository = friendRepository;
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
    }

    /**
     * 发送好友请求
     */
    public FriendRequest sendFriendRequest(String senderId, String receiverId, String message) {
        // 检查是否为自己
        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("不能添加自己为好友");
        }

        // 验证用户存在
        userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("发送者用户不存在"));
        userRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("接收者用户不存在"));

        // 检查是否已经是好友
        if (friendRepository.findByUserIdAndFriendId(senderId, receiverId).isPresent()) {
            throw new IllegalArgumentException("已经是好友关系");
        }

        // 检查是否已经有待处理的请求
        Optional<FriendRequest> existingRequest = requestRepository
                .findBySenderIdAndReceiverIdAndStatus(senderId, receiverId, "pending");
        if (existingRequest.isPresent()) {
            throw new IllegalArgumentException("已经发送过好友请求，等待对方处理中");
        }

        // 创建新的请求
        FriendRequest request = new FriendRequest(senderId, receiverId, message);
        return requestRepository.save(request);
    }

    /**
     * 获取收到的好友请求
     */
    public List<Map<String, Object>> getReceivedFriendRequests(String userId) {
        List<FriendRequest> requests = requestRepository.findByReceiverIdAndStatus(userId, "pending");

        return requests.stream().map(request -> {
            Map<String, Object> requestInfo = new HashMap<>();
            requestInfo.put("requestId", request.getId());
            requestInfo.put("senderId", request.getSenderId());
            requestInfo.put("message", request.getMessage());
            requestInfo.put("createTime", request.getCreateTime());

            // 添加发送者信息
            userRepository.findById(request.getSenderId()).ifPresent(sender -> {
                requestInfo.put("senderUsername", sender.getUsername());
                requestInfo.put("senderAvatar", sender.getAvatarUrl());
            });

            return requestInfo;
        }).collect(Collectors.toList());
    }

    /**
     * 获取发送的好友请求
     */
    public List<Map<String, Object>> getSentFriendRequests(String userId) {
        List<FriendRequest> requests = requestRepository.findBySenderId(userId);

        return requests.stream().map(request -> {
            Map<String, Object> requestInfo = new HashMap<>();
            requestInfo.put("requestId", request.getId());
            requestInfo.put("receiverId", request.getReceiverId());
            requestInfo.put("status", request.getStatus());
            requestInfo.put("message", request.getMessage());
            requestInfo.put("createTime", request.getCreateTime());

            // 添加接收者信息
            userRepository.findById(request.getReceiverId()).ifPresent(receiver -> {
                requestInfo.put("receiverUsername", receiver.getUsername());
                requestInfo.put("receiverAvatar", receiver.getAvatarUrl());
            });

            return requestInfo;
        }).collect(Collectors.toList());
    }

    /**
     * 接受好友请求
     */
    @Transactional
    public Map<String, Object> acceptFriendRequest(Long requestId, String userId) {
        FriendRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("好友请求不存在"));

        // 验证请求是否发给当前用户
        if (!request.getReceiverId().equals(userId)) {
            throw new IllegalArgumentException("无权处理此请求");
        }

        // 验证请求状态
        if (!"pending".equals(request.getStatus())) {
            throw new IllegalArgumentException("该请求已被处理");
        }

        // 更新请求状态
        request.setStatus("accepted");
        request.setUpdateTime(new Date());
        requestRepository.save(request);

        // 创建双向好友关系
        UserFriend friendship1 = new UserFriend(userId, request.getSenderId());
        friendRepository.save(friendship1);

        UserFriend friendship2 = new UserFriend(request.getSenderId(), userId);
        friendRepository.save(friendship2);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("friendId", request.getSenderId());

        // 添加好友信息
        userRepository.findById(request.getSenderId()).ifPresent(friend -> {
            result.put("friendUsername", friend.getUsername());
            result.put("friendAvatar", friend.getAvatarUrl());
        });

        return result;
    }

    /**
     * 拒绝好友请求
     */
    public FriendRequest rejectFriendRequest(Long requestId, String userId) {
        FriendRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("好友请求不存在"));

        // 验证请求是否发给当前用户
        if (!request.getReceiverId().equals(userId)) {
            throw new IllegalArgumentException("无权处理此请求");
        }

        // 验证请求状态
        if (!"pending".equals(request.getStatus())) {
            throw new IllegalArgumentException("该请求已被处理");
        }

        // 更新请求状态
        request.setStatus("rejected");
        request.setUpdateTime(new Date());
        return requestRepository.save(request);
    }

    /**
     * 获取用户的好友列表
     */
    public List<Map<String, Object>> getUserFriends(String userId) {
        List<UserFriend> friendships = friendRepository.findByUserIdAndStatus(userId, "active");

        return friendships.stream().map(friendship -> {
            Map<String, Object> friendInfo = new HashMap<>();
            friendInfo.put("friendshipId", friendship.getId());
            friendInfo.put("friendId", friendship.getFriendId());
            friendInfo.put("nickname", friendship.getNickname());

            // 添加好友详细信息
            userRepository.findById(friendship.getFriendId()).ifPresent(friend -> {
                friendInfo.put("username", friend.getUsername());
                friendInfo.put("avatar", friend.getAvatarUrl());
                friendInfo.put("email", friend.getEmail());
            });

            return friendInfo;
        }).collect(Collectors.toList());
    }

    /**
     * 删除好友
     */
    @Transactional
    public boolean deleteFriend(String userId, String friendId) {
        // 删除双向好友关系
        friendRepository.deleteByUserIdAndFriendId(userId, friendId);
        friendRepository.deleteByUserIdAndFriendId(friendId, userId);
        return true;
    }

    /**
     * 设置好友昵称
     */
    public UserFriend setFriendNickname(String userId, String friendId, String nickname) {
        UserFriend friendship = friendRepository.findByUserIdAndFriendId(userId, friendId)
                .orElseThrow(() -> new IllegalArgumentException("好友关系不存在"));

        friendship.setNickname(nickname);
        friendship.setUpdateTime(new Date());
        return friendRepository.save(friendship);
    }
}