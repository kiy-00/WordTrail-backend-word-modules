package com.tongji.wordtrail.controller;

import com.tongji.wordtrail.service.WordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/words")
@Slf4j
public class WordController {

    private final WordService wordService;

    @Autowired
    public WordController(WordService wordService) {
        this.wordService = wordService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getWord(@PathVariable String id) {
        return wordService.getWord(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getWords(
            @RequestParam(required = false) Map<String, String> queryParams) {
        log.debug("Received query params: {}", queryParams); // 添加日志

        List<Map<String, Object>> results;
        if (queryParams.containsKey("ids")) {
            // 如果查询参数中包含ids，调用getWordsByIds方法
            List<String> ids = Arrays.asList(queryParams.get("ids").split(","));
            results = wordService.getWordsByIds(ids);
        } else {
            // 否则，调用getWords方法
            results = wordService.getWords(queryParams);
        }

        log.debug("Query results: {}", results); // 添加日志
        return ResponseEntity.ok(results);
    }

    @PostMapping("/by-ids")
    public ResponseEntity<List<Map<String, Object>>> getWordsByIds(
            @RequestBody List<String> wordIds) {
        try {
            Query query = new Query(Criteria.where("_id").in(wordIds));
            List<Map<String, Object>> words = wordService.getWordsByIds(wordIds);
            return ResponseEntity.ok(words);
        } catch (Exception e) {
            log.error("Error fetching words by ids: ", e);
            return ResponseEntity.badRequest().build();
        }
    }


    @GetMapping("/page")
    public ResponseEntity<Page<Map<String, Object>>> getWordsWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(wordService.getWordsWithPagination(page, size));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createWord(
            @RequestBody Map<String, Object> wordData) {
        try {
            Map<String, Object> savedWord = wordService.saveWord(wordData);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedWord);
        } catch (Exception e) {
            log.error("Error creating word: ", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/batch")
    public ResponseEntity<List<Map<String, Object>>> createWords(
            @RequestBody List<Map<String, Object>> wordsData) {
        try {
            List<Map<String, Object>> savedWords = wordService.saveWords(wordsData);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedWords);
        } catch (Exception e) {
            log.error("Error creating words batch: ", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateWord(
            @PathVariable String id,
            @RequestBody Map<String, Object> updateData) {
        return wordService.updateWord(id, updateData)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteWord(@PathVariable String id) {
        boolean deleted = wordService.deleteWord(id);
        return deleted ?
                ResponseEntity.ok().build() :
                ResponseEntity.notFound().build();
    }

    @GetMapping("/stats/{field}")
    public ResponseEntity<Map<String, Long>> getWordStats(@PathVariable String field) {
        try {
            return ResponseEntity.ok(wordService.groupByField(field));
        } catch (Exception e) {
            log.error("Error getting word stats: ", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取用于混淆的相似单词
     * 返回4个与目标单词相似的单词ID，这些单词来自系统词库
     */
    @GetMapping("/{id}/confusion-options")
    public ResponseEntity<List<String>> getConfusionOptions(@PathVariable String id) {
        try {
            List<String> confusionWordIds = wordService.generateConfusionOptions(id);
            return ResponseEntity.ok(confusionWordIds);
        } catch (Exception e) {
            log.error("Error generating confusion options for word {}: ", id, e);
            return ResponseEntity.badRequest().build();
        }
    }
}