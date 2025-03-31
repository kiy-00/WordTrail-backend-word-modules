package com.tongji.wordtrail.controller;

import com.tongji.wordtrail.dto.AdminWordResponse;
import com.tongji.wordtrail.dto.AdminWordbooksResponse;
import com.tongji.wordtrail.dto.AuthResponse;
import com.tongji.wordtrail.model.Words;
import com.tongji.wordtrail.service.AdminWordbookService;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/wordbooks")
public class AdminWordbookController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AdminWordbookService adminWordbookService;
    public AdminWordbookController(AdminWordbookService adminWordbookService) {
        this.adminWordbookService = adminWordbookService;
        logger.info("AdminWordbookController initialized");
    }
    @GetMapping("")
    public ResponseEntity<List<AdminWordbooksResponse>> getWordbooks() {
        try {
            List<AdminWordbooksResponse> response = adminWordbookService.findWordBooks();
            logger.info("Find wordbooks successfully, found {} wordbooks", response.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Finding wordbooks failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }
    @GetMapping("/{wordbookId}")
    public ResponseEntity<List<Words>> getWords(@PathVariable ObjectId wordbookId) {
        try {
            List<Words> words = adminWordbookService.findWords(wordbookId);
            return ResponseEntity.ok(words);
        } catch (Exception e) {
            logger.error("Finding wordbooks failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}
