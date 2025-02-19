package com.tongji.wordtrail.controller;

import com.tongji.wordtrail.model.LearningRecord;
import com.tongji.wordtrail.service.LearningRecordService;
import com.tongji.wordtrail.dto.LearningRecordRequest;
import com.tongji.wordtrail.dto.LearningStatsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/learning-records")
@CrossOrigin(origins = "*")
public class LearningRecordController {

    private final LearningRecordService learningRecordService;

    @Autowired
    public LearningRecordController(LearningRecordService learningRecordService) {
        this.learningRecordService = learningRecordService;
    }

    /**
     * 记录用户的学习活动
     * @param userId 用户UUID
     * @param request 学习记录请求
     * @return 保存的学习记录
     */
    @PostMapping("/{userId}")
    public ResponseEntity<LearningRecord> recordActivity(
            @PathVariable String userId,
            @RequestBody LearningRecordRequest request) {
        return ResponseEntity.ok(learningRecordService.recordLearningActivity(
                userId,
                request.getType(),
                request.getWords()
        ));
    }

    @GetMapping("/{userId}/stats")
    public ResponseEntity<LearningStatsResponse> getLearningStats(
            @PathVariable String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endDate) {
        return ResponseEntity.ok(learningRecordService.getUserLearningStats(userId, startDate, endDate));
    }

    @GetMapping("/{userId}/history")
    public ResponseEntity<Page<LearningRecord>> getLearningHistory(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endDate) {
        return ResponseEntity.ok(learningRecordService.getLearningHistory(userId, page, size, startDate, endDate));
    }

    @GetMapping("/{userId}/streak")
    public ResponseEntity<Integer> getConsecutiveDays(@PathVariable String userId) {
        return ResponseEntity.ok(learningRecordService.getConsecutiveLearningDays(userId));
    }

    @GetMapping("/{userId}/success-rate")
    public ResponseEntity<Double> getSuccessRate(
            @PathVariable String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endDate) {
        return ResponseEntity.ok(learningRecordService.getSuccessRate(userId, startDate, endDate));
    }

    @GetMapping("/{userId}/today")
    public ResponseEntity<List<LearningRecord>> getTodayRecords(@PathVariable String userId) {
        return ResponseEntity.ok(learningRecordService.getTodayLearningRecords(userId));
    }
}