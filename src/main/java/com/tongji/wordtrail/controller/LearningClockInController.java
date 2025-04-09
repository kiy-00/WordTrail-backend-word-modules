package com.tongji.wordtrail.controller;

import com.tongji.wordtrail.entity.LearningClockIn;
import com.tongji.wordtrail.model.LearningGoal;
import com.tongji.wordtrail.service.LearningClockInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
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
        try {
            LearningClockIn clockIn = clockInService.tryClockIn(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", clockIn.getStatus());
            response.put("streakDays", clockIn.getStreakDays());
            response.put("newWordsCompleted", clockIn.getNewWordsCompleted());
            response.put("newWordsTarget", clockIn.getNewWordsTarget());
            response.put("reviewWordsCompleted", clockIn.getReviewWordsCompleted());
            response.put("reviewWordsTarget", clockIn.getReviewWordsTarget());

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "打卡失败: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取用户打卡统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getClockInStats(@RequestParam String userId) {
        try {
            Map<String, Object> stats = clockInService.getUserClockInStats(userId);
            return new ResponseEntity<>(stats, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取打卡统计失败: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取用户过去一周的打卡记录
     */
    @GetMapping("/weekly")
    public ResponseEntity<?> getWeeklyClockIn(@RequestParam(required = true) String userId) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "用户ID不能为空");
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
            }

            List<Map<String, Object>> weeklyData = clockInService.getWeeklyClockInHistory(userId);
            return new ResponseEntity<>(weeklyData, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "获取打卡记录失败: " + e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 处理缺少请求参数的异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, String>> handleMissingParams(MissingServletRequestParameterException ex) {
        Map<String, String> response = new HashMap<>();
        String paramName = ex.getParameterName();
        response.put("message", "请求参数 '" + paramName + "' 缺失");
        response.put("error", "参数错误");
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 处理一般异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleExceptions(Exception e) {
        Map<String, String> response = new HashMap<>();
        response.put("error", e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}