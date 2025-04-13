package com.tongji.wordtrail.repository;

import com.tongji.wordtrail.model.UserCheckin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface CheckinRepository extends JpaRepository<UserCheckin, Long> {
    // 1. 获取当前用户当月所有签到记录
    @Query("SELECT uc FROM UserCheckin uc WHERE uc.userId = :userId AND FUNCTION('MONTH', uc.checkinDate) = :month AND FUNCTION('YEAR', uc.checkinDate) = :year")
    List<UserCheckin> findByUserIdAndMonth(@Param("userId") String userId, @Param("month") int month, @Param("year") int year);

    // 2. 判断今天是否已经签到
    @Query("SELECT COUNT(uc) > 0 FROM UserCheckin uc WHERE uc.userId = :userId AND uc.checkinDate = :today")
    boolean hasCheckedInToday(@Param("userId") String userId, @Param("today") Date today);
}
