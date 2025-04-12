package com.tongji.wordtrail.repository;

import com.tongji.wordtrail.model.TeamChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Date;
import java.util.List;

@Repository
public interface TeamChallengeRepository extends JpaRepository<TeamChallenge, Long> {
    // 查找用户参与的所有挑战（作为创建者或伙伴）
    @Query("SELECT tc FROM TeamChallenge tc WHERE tc.creatorId = :userId OR tc.partnerId = :userId")
    List<TeamChallenge> findByUserIdInvolved(@Param("userId") String userId);

    // 查找用户参与的活跃挑战
    @Query("SELECT tc FROM TeamChallenge tc WHERE (tc.creatorId = :userId OR tc.partnerId = :userId) AND tc.status = 'active'")
    List<TeamChallenge> findActiveByUserIdInvolved(@Param("userId") String userId);

    // 查找用户创建的挑战
    List<TeamChallenge> findByCreatorId(String creatorId);

    // 查找用户作为伙伴参与的挑战
    List<TeamChallenge> findByPartnerId(String partnerId);

    // 查找特定状态的挑战
    List<TeamChallenge> findByStatus(String status);

    // 需要添加到TeamChallengeRepository接口中的两个新方法

    /**
     * 查找用户创建的且状态为指定状态的挑战
     */
    List<TeamChallenge> findByCreatorIdAndStatus(String creatorId, String status);

    /**
     * 查找用户作为伙伴且状态为指定状态的挑战
     */
    List<TeamChallenge> findByPartnerIdAndStatus(String partnerId, String status);
}