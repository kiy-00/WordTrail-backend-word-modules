package com.tongji.wordtrail.controller;

import com.tongji.wordtrail.dto.*;
import com.tongji.wordtrail.model.User;
import com.tongji.wordtrail.service.AdminService;
import com.tongji.wordtrail.service.TokenBlacklistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.tongji.wordtrail.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/users/")
public class AdminController {
    @Autowired
    private JwtUtil jwtUtil;
    private TokenBlacklistService tokenBlacklistService;

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private final AdminService adminService;
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
        logger.info("AdminController initialized");//打印日志
    }
    // 用户名密码登录
    @PostMapping("login/account")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        logger.info("Login request for administer: {}", request);
        try {
            AuthResponse response = adminService.login(request);
            logger.info("Login response successfully: {}", response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Login request failed for username: {}, Error:{}", request.getUsername(), e.getMessage(), e);
            throw e;
        }

    }
    // 邮箱密码登录
    @PostMapping("login/email")
    public ResponseEntity<AuthResponse> EmailLogin(@RequestBody EmailLoginRequest request) {
        logger.info("Login request for administer: {}", request);
        try {
            AuthResponse response = adminService.EmailLogin(request);
            logger.info("Login response successfully: {}", response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Login request failed for email: {}, Error:{}", request.getEmail(), e.getMessage(), e);
            throw e;
        }

    }
    // 用户修改密码
    @PostMapping("login/recover")
    public ResponseEntity<AuthResponse> ResetPassword(@RequestBody AdminResetPasswordRequest request) {
        logger.info("Reset password for administer: {}", request);
        try {
            AuthResponse response = adminService.ResetPassword(request);
            logger.info("Reset password response successfully: {}", response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Reset password request failed for account: {}, Error:{}", request.getAccount(), e.getMessage(), e);
            throw e;
        }

    }
    // 查询用户信息
    @PostMapping("profile")
    public ResponseEntity<Map<String, Object>> getUserProfile(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody Map<String, String> requestData) {

        // 从请求体获取 username
        String username = requestData.get("username");
        System.out.println("username: " + username);
        // 检查 Authorization 头是否存在
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "缺少或无效的 token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        // 提取 token 并验证
        String token = authorizationHeader.substring(7);
        String tokenUsername = jwtUtil.extractUsername(token);

        if (tokenUsername == null || !tokenUsername.equals(username) || !jwtUtil.validateToken(token)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "token 无效或已过期");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        // 查询用户信息
        AdminResponse user = adminService.GetAdminInfo(username);
        if (user == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "用户不存在");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        // 返回用户信息
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("userId", user.getUser_id());
        response.put("username", user.getUsername());
        response.put("key", user.getKey());
        response.put("email", user.getEmail());
        response.put("message", "用户信息获取成功");

        return ResponseEntity.ok(response);
    }


    // 用户登出
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Invalid token");
        }

        // 获取 Token
        String token = authHeader.substring(7);

        // 验证 Token
        try {
            String userId = jwtUtil.extractUserId(token);

            // 如果你有 Token 黑名单机制，则加入黑名单
            tokenBlacklistService.addToBlacklist(token);

            logger.info("User with ID {} logged out successfully. Token added to blacklist.", userId);
            return ResponseEntity.ok("Logout successful");
        } catch (Exception e) {
            logger.error("Invalid token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }
    }



}
