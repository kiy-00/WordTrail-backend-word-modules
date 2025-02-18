package com.tongji.wordtrail.controller;

import com.tongji.wordtrail.service.SystemWordbookService;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/system-wordbooks")
@Slf4j
public class SystemWordbookController {

    private final SystemWordbookService systemWordbookService;

    @Autowired
    public SystemWordbookController(SystemWordbookService systemWordbookService) {
        this.systemWordbookService = systemWordbookService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getWordbook(@PathVariable String id) {
        return systemWordbookService.getSystemWordbook(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<Page<Map<String, Object>>> getWordbooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Map<String, String> filters) {
        return ResponseEntity.ok(systemWordbookService.getSystemWordbooks(page, size, filters));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createWordbook(
            @RequestBody Map<String, Object> wordbookData) {
        try {
            Map<String, Object> savedWordbook = systemWordbookService.createSystemWordbook(wordbookData);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedWordbook);
        } catch (Exception e) {
            log.error("Error creating system wordbook: ", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateWordbook(
            @PathVariable String id,
            @RequestBody Map<String, Object> updateData) {
        return systemWordbookService.updateSystemWordbook(id, updateData)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteWordbook(@PathVariable String id) {
        boolean deleted = systemWordbookService.deleteSystemWordbook(id);
        return deleted ?
                ResponseEntity.ok().build() :
                ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/words")
    public ResponseEntity<?> addWords(
            @PathVariable String id,
            @RequestBody List<String> wordIds) {
        try {
            // 记录输入参数
            log.info("Attempting to add words to wordbook. Wordbook ID: {}", id);
            log.info("Word IDs to add: {}", wordIds);

            // 记录请求的详细信息
            if (wordIds == null || wordIds.isEmpty()) {
                log.warn("Word IDs list is null or empty");
                return ResponseEntity.badRequest().build();
            }

            // 添加单词并记录结果
            Optional<?> result = systemWordbookService.addWordsToWordbook(id, wordIds);

            if (result.isPresent()) {
                log.info("Successfully added words to wordbook {}", id);
                return ResponseEntity.ok(result.get());
            } else {
                log.warn("Wordbook not found with ID: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            // 记录详细的错误信息
            log.error("Error adding words to wordbook. Wordbook ID: {}, Word IDs: {}", id, wordIds);
            log.error("Error details: ", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}/words")
    public ResponseEntity<?> removeWords(
            @PathVariable String id,
            @RequestBody List<String> wordIds) {

        // 将字符串 ID 转换为 ObjectId
        List<ObjectId> objectIds = wordIds.stream()
                .map(ObjectId::new)
                .collect(Collectors.toList());

        return systemWordbookService.removeWordsFromWordbook(id, objectIds)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/words")
    public ResponseEntity<List<Map<String, Object>>> getWordbookWords(@PathVariable String id) {
        return ResponseEntity.ok(systemWordbookService.getWordbookWords(id));
    }
}