package com.tongji.wordtrail.repository;

import com.tongji.wordtrail.dto.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    List<FriendRequest> findBySenderId(String senderId);
    List<FriendRequest> findByReceiverId(String receiverId);
    List<FriendRequest> findByReceiverIdAndStatus(String receiverId, String status);
    Optional<FriendRequest> findBySenderIdAndReceiverIdAndStatus(String senderId, String receiverId, String status);
}