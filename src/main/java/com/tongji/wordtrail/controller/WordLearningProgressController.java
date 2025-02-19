package com.tongji.wordtrail.controller;

import com.tongji.wordtrail.model.WordLearningProgress;
import com.tongji.wordtrail.service.WordLearningProgressService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 单词学习进度控制器
 * 处理用户的单词学习和复习相关请求
 */
@RestController
@RequestMapping("/api/v1/learning")
public class WordLearningProgressController {

    private final WordLearningProgressService learningProgressService;

    @Autowired
    public WordLearningProgressController(WordLearningProgressService learningProgressService) {
        this.learningProgressService = learningProgressService;
    }

    /**
     * 开始学习新单词
     */
    @PostMapping("/start")
    public ResponseEntity<WordLearningProgress> startLearning(
            @RequestParam String userId,
            @RequestParam ObjectId wordId) {
        WordLearningProgress progress = learningProgressService.startLearningWord(userId, wordId);
        return new ResponseEntity<>(progress, HttpStatus.OK);
    }

    /**
     * 记录复习结果
     */
    @PostMapping("/review")
    public ResponseEntity<WordLearningProgress> recordReview(
            @RequestParam String userId,
            @RequestParam ObjectId wordId,
            @RequestParam boolean remembered) {
        WordLearningProgress progress = learningProgressService.recordReviewResult(userId, wordId, remembered);
        if (progress == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(progress, HttpStatus.OK);
    }

    /**
     * 获取今日需要复习的单词
     */
    @GetMapping("/today-review")
    public ResponseEntity<List<WordLearningProgress>> getTodayReviewWords(
            @RequestParam String userId,
            @RequestParam(required = false) ObjectId bookId) {
        List<WordLearningProgress> words = bookId != null ?
                learningProgressService.getTodayReviewWordsForBook(userId, bookId) :
                learningProgressService.getTodayReviewWords(userId);
        return new ResponseEntity<>(words, HttpStatus.OK);
    }

    /**
     * 获取待复习单词数量
     */
    @GetMapping("/overdue-count")
    public ResponseEntity<Map<String, Long>> getOverdueReviewCount(
            @RequestParam String userId,
            @RequestParam(required = false) ObjectId bookId) {
        long count = bookId != null ?
                learningProgressService.getOverdueReviewCountForBook(userId, bookId) :
                learningProgressService.getOverdueReviewCount(userId);
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 获取用户总体学习统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<WordLearningProgressService.UserLearningStats> getUserStats(
            @RequestParam String userId) {
        WordLearningProgressService.UserLearningStats stats =
                learningProgressService.getUserStats(userId);
        return new ResponseEntity<>(stats, HttpStatus.OK);
    }

    /**
     * 获取词书的学习统计信息
     */
    @GetMapping("/book/{bookId}/stats")
    public ResponseEntity<WordLearningProgressService.BookLearningStats> getBookStats(
            @RequestParam String userId,
            @PathVariable ObjectId bookId) {
        WordLearningProgressService.BookLearningStats stats =
                learningProgressService.getBookStats(userId, bookId);
        return new ResponseEntity<>(stats, HttpStatus.OK);
    }

    /**
     * 获取词书中需要复习的单词
     */
    @GetMapping("/book/{bookId}/review")
    public ResponseEntity<List<WordLearningProgress>> getBookReviewWords(
            @RequestParam String userId,
            @PathVariable ObjectId bookId) {
        List<WordLearningProgress> words = learningProgressService.getBookReviewWords(userId, bookId);
        return new ResponseEntity<>(words, HttpStatus.OK);
    }

    /**
     * 获取词书中所有单词的学习进度
     */
    @GetMapping("/book/{bookId}/progress")
    public ResponseEntity<List<WordLearningProgress>> getBookProgress(
            @RequestParam String userId,
            @PathVariable ObjectId bookId) {
        List<WordLearningProgress> progress =
                learningProgressService.getProgressForBook(userId, bookId);
        return new ResponseEntity<>(progress, HttpStatus.OK);
    }

    /**
     * 获取单个单词的学习进度
     */
    @GetMapping("/progress/{wordId}")
    public ResponseEntity<WordLearningProgress> getWordProgress(
            @RequestParam String userId,
            @PathVariable ObjectId wordId) {
        return learningProgressService.getWordProgress(userId, wordId)
                .map(progress -> new ResponseEntity<>(progress, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * 批量获取单词学习进度
     */
    @PostMapping("/progress/batch")
    public ResponseEntity<List<WordLearningProgress>> getProgressBatch(
            @RequestParam String userId,
            @RequestBody List<ObjectId> wordIds) {
        List<WordLearningProgress> progress =
                learningProgressService.getProgressForWords(userId, wordIds);
        return new ResponseEntity<>(progress, HttpStatus.OK);
    }

    /**
     * 异常处理
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
        Map<String, String> response = new HashMap<>();
        response.put("error", e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}