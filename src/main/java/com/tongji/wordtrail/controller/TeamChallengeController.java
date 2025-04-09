package com.tongji.wordtrail.controller;

import com.tongji.wordtrail.model.TeamChallenge;
import com.tongji.wordtrail.service.TeamChallengeService;
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
            @RequestParam String creatorId,
            @RequestParam String partnerId,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam int dailyWordsTarget,
            @RequestParam int durationDays) {
        try {
            TeamChallenge challenge = challengeService.createChallenge(
                    creatorId, partnerId, name, description, dailyWordsTarget, durationDays);
            return ResponseEntity.ok(challenge);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "创建挑战失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 接受组队挑战
     */
    @PostMapping("/{challengeId}/accept")
    public ResponseEntity<?> acceptChallenge(
            @PathVariable Long challengeId,
            @RequestParam String userId) {
        try {
            TeamChallenge challenge = challengeService.acceptChallenge(challengeId, userId);
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
    public ResponseEntity<?> rejectChallenge(
            @PathVariable Long challengeId,
            @RequestParam String userId) {
        try {
            TeamChallenge challenge = challengeService.rejectChallenge(challengeId, userId);
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
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserChallenges(@PathVariable String userId) {
        try {
            List<Map<String, Object>> challenges = challengeService.getUserChallenges(userId);
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
    public ResponseEntity<?> getChallengeDetail(
            @PathVariable Long challengeId,
            @RequestParam String userId) {
        try {
            Map<String, Object> challenge = challengeService.getChallengeDetail(challengeId, userId);
            return ResponseEntity.ok(challenge);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "获取挑战详情失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 组队打卡
     */
    @PostMapping("/{challengeId}/clock-in")
    public ResponseEntity<?> clockIn(
            @PathVariable Long challengeId,
            @RequestParam String userId,
            @RequestParam int wordsCompleted) {
        try {
            Map<String, Object> result = challengeService.clockIn(challengeId, userId, wordsCompleted);
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
    @GetMapping("/active/user/{userId}")
    public ResponseEntity<?> getActiveUserChallenges(@PathVariable String userId) {
        try {
            List<Map<String, Object>> challenges = challengeService.getActiveUserChallenges(userId);
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
    public ResponseEntity<?> getChallengeStats(
            @PathVariable Long challengeId,
            @RequestParam String userId) {
        try {
            Map<String, Object> stats = challengeService.getChallengeStats(challengeId, userId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "获取统计信息失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}