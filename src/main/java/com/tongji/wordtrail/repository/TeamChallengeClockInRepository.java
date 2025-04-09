package com.tongji.wordtrail.repository;

import com.tongji.wordtrail.model.TeamChallengeClockIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface TeamChallengeClockInRepository extends JpaRepository<TeamChallengeClockIn, Long> {
    // 根据挑战ID和用户ID查找特定日期的打卡记录
    Optional<TeamChallengeClockIn> findByChallengeIdAndUserIdAndClockInDate(Long challengeId, String userId, Date clockInDate);

    // 查找用户在特定挑战中的所有打卡记录
    List<TeamChallengeClockIn> findByChallengeIdAndUserId(Long challengeId, String userId);

    // 查找挑战的所有打卡记录
    List<TeamChallengeClockIn> findByChallengeId(Long challengeId);

    // 查找特定日期的所有打卡记录
    List<TeamChallengeClockIn> findByClockInDate(Date clockInDate);

    // 查找用户在特定日期的所有打卡记录（跨多个挑战）
    List<TeamChallengeClockIn> findByUserIdAndClockInDate(String userId, Date clockInDate);

    // 计算挑战中特定用户的连续打卡天数
    @Query("SELECT COUNT(DISTINCT tcci.clockInDate) FROM TeamChallengeClockIn tcci " +
            "WHERE tcci.challengeId = :challengeId AND tcci.userId = :userId AND tcci.status = true")
    int countSuccessfulClockInDays(@Param("challengeId") Long challengeId, @Param("userId") String userId);

    // 检查特定日期是否两人都已打卡成功
    @Query("SELECT COUNT(tcci) FROM TeamChallengeClockIn tcci " +
            "WHERE tcci.challengeId = :challengeId AND tcci.clockInDate = :date AND tcci.status = true")
    int countSuccessfulClockInsForDate(@Param("challengeId") Long challengeId, @Param("date") Date date);
}