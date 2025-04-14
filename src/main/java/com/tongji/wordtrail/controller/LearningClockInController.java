package com.tongji.wordtrail.controller;

import com.tongji.wordtrail.entity.LearningClockIn;
import com.tongji.wordtrail.model.LearningGoal;
import com.tongji.wordtrail.service.LearningClockInService;
import com.tongji.wordtrail.util.JwtUtil;
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
    public ResponseEntity<?> setLearningGoal(
            @RequestParam int dailyNewWordsGoal,
            @RequestParam int dailyReviewWordsGoal) {
        try {
            // 获取当前认证用户ID
            String userId = JwtUtil.getCurrentUserId();
            if (userId == null) {
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put("message", "未认证的用户");
                errorMap.put("code", "UNAUTHORIZED");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMap);
            }

            LearningGoal goal = clockInService.setLearningGoal(userId, dailyNewWordsGoal, dailyReviewWordsGoal);
            return new ResponseEntity<>(goal, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "设置学习目标失败: " + e.getMessage());
            error.put("code", "SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 获取学习目标
     */
    @GetMapping("/goal")
    public ResponseEntity<?> getLearningGoal() {
        try {
            // 获取当前认证用户ID
            String userId = JwtUtil.getCurrentUserId();
            if (userId == null) {
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put("message", "未认证的用户");
                errorMap.put("code", "UNAUTHORIZED");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMap);
            }

            LearningGoal goal = clockInService.getLearningGoal(userId);
            return new ResponseEntity<>(goal, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "获取学习目标失败: " + e.getMessage());
            error.put("code", "SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 获取今日打卡状态 - 修改为获取实时数据
     */
    @GetMapping("/today")
    public ResponseEntity<?> getTodayClockIn() {
        try {
            // 获取当前认证用户ID
            String userId = JwtUtil.getCurrentUserId();
            if (userId == null) {
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put("message", "未认证的用户");
                errorMap.put("code", "UNAUTHORIZED");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMap);
            }

            // 修改为使用实时更新方法
            LearningClockIn clockIn = clockInService.getAndUpdateTodayClockIn(userId);
            return new ResponseEntity<>(clockIn, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "获取今日打卡状态失败: " + e.getMessage());
            error.put("code", "SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 执行打卡操作
     */
    @PostMapping("/try")
    public ResponseEntity<Map<String, Object>> tryClockIn() {
        try {
            // 获取当前认证用户ID
            String userId = JwtUtil.getCurrentUserId();
            if (userId == null) {
                Map<String, Object> errorMap = new HashMap<>();
                errorMap.put("success", false);
                errorMap.put("message", "未认证的用户");
                errorMap.put("code", "UNAUTHORIZED");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMap);
            }

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
            errorResponse.put("message", "打卡失败: " + e.getMessage());
            errorResponse.put("code", "SERVER_ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取用户打卡统计信息 - 修改为获取实时数据
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getClockInStats() {
        try {
            // 获取当前认证用户ID
            String userId = JwtUtil.getCurrentUserId();
            if (userId == null) {
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put("message", "未认证的用户");
                errorMap.put("code", "UNAUTHORIZED");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMap);
            }

            // 修改为使用实时更新方法
            Map<String, Object> stats = clockInService.getUpdatedUserClockInStats(userId);
            return new ResponseEntity<>(stats, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "获取打卡统计失败: " + e.getMessage());
            errorResponse.put("code", "SERVER_ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取用户过去一周的打卡记录 - 修改为获取实时数据
     */
    @GetMapping("/weekly")
    public ResponseEntity<?> getWeeklyClockIn() {
        try {
            // 获取当前认证用户ID
            String userId = JwtUtil.getCurrentUserId();
            if (userId == null) {
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put("message", "未认证的用户");
                errorMap.put("code", "UNAUTHORIZED");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMap);
            }

            // 修改为使用实时更新方法
            List<Map<String, Object>> weeklyData = clockInService.getUpdatedWeeklyClockInHistory(userId);
            return new ResponseEntity<>(weeklyData, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "获取打卡记录失败: " + e.getMessage());
            error.put("code", "SERVER_ERROR");
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