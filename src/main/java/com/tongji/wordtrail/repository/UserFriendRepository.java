package com.tongji.wordtrail.repository;

import com.tongji.wordtrail.model.UserFriend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserFriendRepository extends JpaRepository<UserFriend, Long> {
    List<UserFriend> findByUserIdAndStatus(String userId, String status);
    Optional<UserFriend> findByUserIdAndFriendId(String userId, String friendId);
    void deleteByUserIdAndFriendId(String userId, String friendId);
}