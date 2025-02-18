package com.tongji.wordtrail.controller;

import com.tongji.wordtrail.model.WordLearningProgress;
import com.tongji.wordtrail.service.WordLearningProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/progress")
public class WordLearningProgressController {

    private final WordLearningProgressService progressService;

    @Autowired
    public WordLearningProgressController(WordLearningProgressService progressService) {
        this.progressService = progressService;
    }

    @GetMapping("/word/{userId}/{wordId}")
    public ResponseEntity<?> getWordProgress(
            @PathVariable String userId,
            @PathVariable String wordId) {
        return ResponseEntity.ok(progressService.getWordProgress(userId, wordId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserProgress(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0.7") double proficiencyThreshold) {
        return ResponseEntity.ok(progressService.getUserWordProgress(userId, proficiencyThreshold));
    }

    @PostMapping("/update/{userId}/{wordId}")
    public ResponseEntity<?> updateProgress(
            @PathVariable String userId,
            @PathVariable String wordId,
            @RequestParam double proficiency) {
        return ResponseEntity.ok(progressService.updateWordProgress(userId, wordId, proficiency));
    }

    @GetMapping("/review/{userId}")
    public ResponseEntity<?> getWordsForReview(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0.7") double proficiencyThreshold,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date lastReviewBefore) {
        return ResponseEntity.ok(progressService.getWordsNeedingReview(userId, proficiencyThreshold, lastReviewBefore));
    }

    @GetMapping("/mastered/{userId}")
    public ResponseEntity<?> getMasteredWords(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0.9") double masteryThreshold) {
        return ResponseEntity.ok(progressService.getMasteredWords(userId, masteryThreshold));
    }

    @GetMapping("/average/{userId}")
    public ResponseEntity<?> getAverageProficiency(@PathVariable String userId) {
        return ResponseEntity.ok(progressService.calculateAverageProficiency(userId));
    }

    @PostMapping("/batch/{userId}")
    public ResponseEntity<?> getProgressForWords(
            @PathVariable String userId,
            @RequestBody List<String> wordIds) {
        return ResponseEntity.ok(progressService.getProgressForWords(userId, wordIds));
    }
}