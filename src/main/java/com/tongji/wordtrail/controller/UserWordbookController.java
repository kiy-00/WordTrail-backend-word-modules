package com.tongji.wordtrail.controller;

import com.tongji.wordtrail.service.UserWordbookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/user-wordbooks")
@Slf4j
public class UserWordbookController {

    private final UserWordbookService userWordbookService;

    @Autowired
    public UserWordbookController(UserWordbookService userWordbookService) {
        this.userWordbookService = userWordbookService;
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> getWordbook(@PathVariable String id) {
        return userWordbookService.getUserWordbook(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<Map<String, Object>>> getUserWordbooks(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Map<String, String> filters) {
        return ResponseEntity.ok(userWordbookService.getUserWordbooks(userId, page, size, filters));
    }

    @GetMapping("/public")
    public ResponseEntity<Page<Map<String, Object>>> getPublicWordbooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userWordbookService.getPublicWordbooks(page, size));
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> createWordbook(
            @PathVariable String userId,
            @RequestBody Map<String, Object> wordbookData) {
        try {
            Map<String, Object> savedWordbook = userWordbookService.createUserWordbook(userId, wordbookData);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedWordbook);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error creating user wordbook: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}/user/{userId}")
    public ResponseEntity<?> updateWordbook(
            @PathVariable String id,
            @PathVariable String userId,
            @RequestBody Map<String, Object> updateData) {
        return userWordbookService.updateUserWordbook(id, userId, updateData)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/user/{userId}")
    public ResponseEntity<?> deleteWordbook(
            @PathVariable String id,
            @PathVariable String userId) {
        boolean deleted = userWordbookService.deleteUserWordbook(id, userId);
        return deleted ?
                ResponseEntity.ok().build() :
                ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable String id,
            @RequestParam String status) {
        try {
            return userWordbookService.updateWordbookStatus(id, status)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/words/user/{userId}")
    public ResponseEntity<?> addWords(
            @PathVariable String id,
            @PathVariable String userId,
            @RequestBody List<String> wordIds) {
        try {
            return userWordbookService.addWordsToWordbook(id, userId, wordIds)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error adding words to wordbook: ", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}/words/user/{userId}")
    public ResponseEntity<?> removeWords(
            @PathVariable String id,
            @PathVariable String userId,
            @RequestBody List<String> wordIds) {
        try {
            return userWordbookService.removeWordsFromWordbook(id, userId, wordIds)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error removing words from wordbook: ", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}/words/user/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getWordbookWords(
            @PathVariable String id,
            @PathVariable String userId) {
        try {
            return ResponseEntity.ok(userWordbookService.getWordbookWords(id, userId));
        } catch (Exception e) {
            log.error("Error getting wordbook words: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 获取公开词书的单词（不需要用户ID）
    @GetMapping("/{id}/words")
    public ResponseEntity<List<Map<String, Object>>> getPublicWordbookWords(
            @PathVariable String id) {
        try {
            return ResponseEntity.ok(userWordbookService.getWordbookWords(id, null));
        } catch (Exception e) {
            log.error("Error getting public wordbook words: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/by-language/{language}/approved")
    public ResponseEntity<List<Map<String, Object>>> getApprovedPublicWordbooksByLanguage(
            @PathVariable String language) {
        try {
            log.info("Fetching approved public wordbooks by language: {}", language);
            List<Map<String, Object>> wordbooks = userWordbookService.getApprovedPublicWordbooksByLanguage(language);
            return ResponseEntity.ok(wordbooks);
        } catch (Exception e) {
            log.error("Error fetching approved public wordbooks by language: {}", language, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 检查指定单词是否存在于用户的任何词书中
     * @param userId 用户ID
     * @param wordId 单词ID
     * @return 包含检查结果的Map
     */
    @GetMapping("/user/{userId}/check-word/{wordId}")
    public ResponseEntity<Map<String, Object>> checkWordExistsInUserWordbooks(
            @PathVariable String userId,
            @PathVariable String wordId) {
        try {
            Map<String, Object> result = userWordbookService.checkWordExistsInUserWordbooks(userId, wordId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error checking if word exists in user wordbooks: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}