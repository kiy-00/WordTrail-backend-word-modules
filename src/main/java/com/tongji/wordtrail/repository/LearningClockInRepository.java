package com.tongji.wordtrail.repository;

import com.tongji.wordtrail.entity.LearningClockIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.Optional;
import java.util.List;

public interface LearningClockInRepository extends JpaRepository<LearningClockIn, Integer> {

    // 使用原生SQL查询，确保日期比较正确
    @Query(value = "SELECT * FROM learning_clock_in WHERE user_id = :userId AND DATE(clock_in_date) = DATE(:date)",
            nativeQuery = true)
    Optional<LearningClockIn> findByUserIdAndDate(@Param("userId") String userId, @Param("date") Date date);

    // 查找用户最近的打卡记录
    Optional<LearningClockIn> findTopByUserIdOrderByClockInDateDesc(String userId);

    // 查找用户所有打卡记录
    List<LearningClockIn> findByUserIdOrderByClockInDateDesc(String userId);
    /**
     * 查找特定用户的所有打卡记录
     */
    List<LearningClockIn> findByUserId(String userId);
}