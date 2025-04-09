package com.tongji.wordtrail.repository;

import com.tongji.wordtrail.entity.LearningClockIn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.Optional;

public interface LearningClockInRepository extends JpaRepository<LearningClockIn, Integer> {

    // 根据用户ID和日期查找打卡记录
    Optional<LearningClockIn> findByUserIdAndClockInDate(String userId, Date clockInDate);

    // 查找用户最近的打卡记录
    Optional<LearningClockIn> findTopByUserIdOrderByClockInDateDesc(String userId);

    // 统计用户连续打卡天数（可选）
    Long countByUserIdAndStatusTrueAndClockInDateGreaterThanEqual(String userId, Date startDate);
}