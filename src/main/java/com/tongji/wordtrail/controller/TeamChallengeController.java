package com.tongji.wordtrail.controller;

import com.tongji.wordtrail.model.TeamChallenge;
import com.tongji.wordtrail.service.TeamChallengeService;
import com.tongji.wordtrail.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/team-challenges")
public class TeamChallengeController {
    private final TeamChallengeService challengeService;

    @Autowired
    public TeamChallengeController(TeamChallengeService challengeService) {
        this.challengeService = challengeService;
    }

    /**
     * 创建组队挑战
     */
    @PostMapping("/create")
    public ResponseEntity<?> createChallenge(
            @RequestParam String partnerId,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam int dailyWordsTarget,
            @RequestParam int durationDays) {
        try {
            // 获取当前认证用户ID
            String currentUserId = JwtUtil.getCurrentUserId();
            if (currentUserId == null) {
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put("message", "未认证的用户");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMap);
            }

            TeamChallenge challenge = challengeService.createChallenge(
                    currentUserId, partnerId, name, description, dailyWordsTarget, durationDays);
            return ResponseEntity.ok(challenge);
        } catch (IllegalArgumentException e) {
            // 这是业务规则验证失败，应返回400 Bad Request
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            // 其他未预期的异常，返回500
            Map<String, String> error = new HashMap<>();
            error.put("message", "创建挑战失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 接受组队挑战
     */
    @PostMapping("/{challengeId}/accept")
    public ResponseEntity<?> acceptChallenge(@PathVariable Long challengeId) {
        try {
            // 获取当前认证用户ID
            String currentUserId = JwtUtil.getCurrentUserId();
            if (currentUserId == null) {
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put("message", "未认证的用户");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMap);
            }

            TeamChallenge challenge = challengeService.acceptChallenge(challengeId, currentUserId);
            return ResponseEntity.ok(challenge);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "接受挑战失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 拒绝组队挑战
     */
    @PostMapping("/{challengeId}/reject")
    public ResponseEntity<?> rejectChallenge(@PathVariable Long challengeId) {
        try {
            // 获取当前认证用户ID
            String currentUserId = JwtUtil.getCurrentUserId();
            if (currentUserId == null) {
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put("message", "未认证的用户");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMap);
            }

            TeamChallenge challenge = challengeService.rejectChallenge(challengeId, currentUserId);
            return ResponseEntity.ok(challenge);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "拒绝挑战失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 获取用户参与的所有挑战
     */
    @GetMapping("/user")
    public ResponseEntity<?> getUserChallenges() {
        try {
            // 获取当前认证用户ID
            String currentUserId = JwtUtil.getCurrentUserId();
            if (currentUserId == null) {
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put("message", "未认证的用户");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMap);
            }

            List<Map<String, Object>> challenges = challengeService.getUserChallenges(currentUserId);
            return ResponseEntity.ok(challenges);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "获取挑战列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 获取单个挑战详情
     */
    @GetMapping("/{challengeId}")
    public ResponseEntity<?> getChallengeDetail(@PathVariable Long challengeId) {
        try {
            // 获取当前认证用户ID
            String currentUserId = JwtUtil.getCurrentUserId();
            if (currentUserId == null) {
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put("message", "未认证的用户");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMap);
            }

            Map<String, Object> challenge = challengeService.getChallengeDetail(challengeId, currentUserId);
            return ResponseEntity.ok(challenge);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "获取挑战详情失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 组队打卡 - 使用系统记录的实际学习数据
     */
    @PostMapping("/{challengeId}/clock-in")
    public ResponseEntity<?> clockIn(@PathVariable Long challengeId) {
        try {
            // 获取当前认证用户ID
            String currentUserId = JwtUtil.getCurrentUserId();
            if (currentUserId == null) {
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put("message", "未认证的用户");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMap);
            }

            Map<String, Object> result = challengeService.clockIn(challengeId, currentUserId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "打卡失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 获取用户的活跃挑战
     */
    @GetMapping("/active")
    public ResponseEntity<?> getActiveUserChallenges() {
        try {
            // 获取当前认证用户ID
            String currentUserId = JwtUtil.getCurrentUserId();
            if (currentUserId == null) {
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put("message", "未认证的用户");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMap);
            }

            List<Map<String, Object>> challenges = challengeService.getActiveUserChallenges(currentUserId);
            return ResponseEntity.ok(challenges);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "获取活跃挑战失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 获取挑战统计信息
     */
    @GetMapping("/{challengeId}/stats")
    public ResponseEntity<?> getChallengeStats(@PathVariable Long challengeId) {
        try {
            // 获取当前认证用户ID
            String currentUserId = JwtUtil.getCurrentUserId();
            if (currentUserId == null) {
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put("message", "未认证的用户");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMap);
            }

            Map<String, Object> stats = challengeService.getChallengeStats(challengeId, currentUserId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "获取统计信息失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 获取当日打卡状态
     */
    @GetMapping("/{challengeId}/today-status")
    public ResponseEntity<?> getTodayClockInStatus(@PathVariable Long challengeId) {
        try {
            String currentUserId = JwtUtil.getCurrentUserId();
            if (currentUserId == null) {
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put("message", "未认证的用户");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMap);
            }

            Map<String, Object> status = challengeService.getTodayClockInStatus(challengeId, currentUserId);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "获取当日打卡状态失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 获取挑战最终结果
     */
    @GetMapping("/{challengeId}/result")
    public ResponseEntity<?> getChallengeResult(@PathVariable Long challengeId) {
        try {
            String currentUserId = JwtUtil.getCurrentUserId();
            if (currentUserId == null) {
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put("message", "未认证的用户");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMap);
            }

            Map<String, Object> result = challengeService.getChallengeResult(challengeId, currentUserId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "获取挑战结果失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}