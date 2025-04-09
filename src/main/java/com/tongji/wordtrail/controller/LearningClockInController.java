package com.tongji.wordtrail.controller;

import com.tongji.wordtrail.entity.LearningClockIn;
import com.tongji.wordtrail.model.LearningGoal;
import com.tongji.wordtrail.service.LearningClockInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/clock-in")
public class LearningClockInController {

    private final LearningClockInService clockInService;

    @Autowired
    public LearningClockInController(LearningClockInService clockInService) {
        this.clockInService = clockInService;
    }

    /**
     * 设置学习目标
     */
    @PostMapping("/goal")
    public ResponseEntity<LearningGoal> setLearningGoal(
            @RequestParam String userId,
            @RequestParam int dailyNewWordsGoal,
            @RequestParam int dailyReviewWordsGoal) {
        LearningGoal goal = clockInService.setLearningGoal(userId, dailyNewWordsGoal, dailyReviewWordsGoal);
        return new ResponseEntity<>(goal, HttpStatus.OK);
    }

    /**
     * 获取学习目标
     */
    @GetMapping("/goal")
    public ResponseEntity<LearningGoal> getLearningGoal(@RequestParam String userId) {
        LearningGoal goal = clockInService.getLearningGoal(userId);
        return new ResponseEntity<>(goal, HttpStatus.OK);
    }

    /**
     * 获取今日打卡状态
     */
    @GetMapping("/today")
    public ResponseEntity<LearningClockIn> getTodayClockIn(@RequestParam String userId) {
        LearningClockIn clockIn = clockInService.getOrCreateTodayClockIn(userId);
        return new ResponseEntity<>(clockIn, HttpStatus.OK);
    }

    /**
     * 执行打卡操作
     */
    @PostMapping("/try")
    public ResponseEntity<Map<String, Object>> tryClockIn(@RequestParam String userId) {
        LearningClockIn clockIn = clockInService.tryClockIn(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", clockIn.getStatus());
        response.put("streakDays", clockIn.getStreakDays());
        response.put("newWordsCompleted", clockIn.getNewWordsCompleted());
        response.put("newWordsTarget", clockIn.getNewWordsTarget());
        response.put("reviewWordsCompleted", clockIn.getReviewWordsCompleted());
        response.put("reviewWordsTarget", clockIn.getReviewWordsTarget());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 获取用户打卡统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getClockInStats(@RequestParam String userId) {
        Map<String, Object> stats = clockInService.getUserClockInStats(userId);
        return new ResponseEntity<>(stats, HttpStatus.OK);
    }

    /**
     * 获取用户过去一周的打卡记录
     */
    @GetMapping("/weekly")
    public ResponseEntity<List<Map<String, Object>>> getWeeklyClockIn(@RequestParam String userId) {
        List<Map<String, Object>> weeklyData = clockInService.getWeeklyClockInHistory(userId);
        return new ResponseEntity<>(weeklyData, HttpStatus.OK);
    }

    /**
     * 异常处理
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleExceptions(Exception e) {
        Map<String, String> response = new HashMap<>();
        response.put("error", e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}