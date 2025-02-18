package com.tongji.wordtrail.controller;

import com.tongji.wordtrail.model.LearningRecord;
import com.tongji.wordtrail.service.LearningRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/api/v1/learning-records")
public class LearningRecordController {

    private final LearningRecordService learningRecordService;

    @Autowired
    public LearningRecordController(LearningRecordService learningRecordService) {
        this.learningRecordService = learningRecordService;
    }

    @PostMapping("/{userId}")
    public ResponseEntity<LearningRecord> recordActivity(
            @PathVariable String userId,
            @RequestParam String type,
            @RequestParam int count) {
        return ResponseEntity.ok(learningRecordService.recordLearningActivity(userId, type, count));
    }

    @GetMapping("/{userId}/stats")
    public ResponseEntity<?> getLearningStats(
            @PathVariable String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endDate) {
        return ResponseEntity.ok(learningRecordService.getUserLearningStats(userId, startDate, endDate));
    }

    @GetMapping("/{userId}/history")
    public ResponseEntity<?> getLearningHistory(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(learningRecordService.getLearningHistory(userId, page, size));
    }

    @GetMapping("/{userId}/streak")
    public ResponseEntity<Integer> getConsecutiveDays(@PathVariable String userId) {
        return ResponseEntity.ok(learningRecordService.getConsecutiveLearningDays(userId));
    }
}