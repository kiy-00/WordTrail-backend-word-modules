package com.tongji.wordtrail.repository;

import com.tongji.wordtrail.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    // 需要添加到UserRepository接口中的新方法

    // 需要添加到UserRepository接口中的新方法

    /**
     * 查找用户名包含指定关键词的用户
     */
    List<User> findByUsernameContaining(String keyword);

    /**
     * 查找ID不在指定列表中的用户（用于推荐）
     */
    @Query(value = "SELECT * FROM user u WHERE u.user_id NOT IN :excludeIds ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<User> findUsersNotInIds(@Param("excludeIds") List<String> excludeIds, @Param("limit") int limit);
}