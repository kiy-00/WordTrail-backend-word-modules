package com.tongji.wordtrail.service;

import com.tongji.wordtrail.dto.*;
import com.tongji.wordtrail.exception.InvalidCredentialsException;
import com.tongji.wordtrail.model.Administer;
import com.tongji.wordtrail.repository.AdminRepository;
import com.tongji.wordtrail.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminService {
    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);
    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    // 用户名密码登录
    public AuthResponse login(LoginRequest request) {
        logger.debug("Processing login for username: {}", request.getUsername());
        // 使用 Optional 来避免 null 检查
        Administer admin = adminRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));
        
        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            logger.warn("Invalid password for username");
            throw new RuntimeException("Invalid credentials");
        }
        logger.info("User logged in successfully");

        String token = jwtUtil.generateToken(admin.getUserId(), admin.getUsername());
        return new AuthResponse(token, admin.getUserId(), admin.getUsername());
    }
    // 邮箱登录
    public AuthResponse EmailLogin(EmailLoginRequest request) {
        logger.debug("Processing login for email: {}", request.getEmail());

        // 查找用户
        Administer admin = adminRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            logger.warn("Invalid password for given email.");
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // 生成 JWT Token
        String token = jwtUtil.generateToken(admin.getUserId(), admin.getEmail());

        logger.info("User logged in successfully.");
        return new AuthResponse(token, admin.getUserId(), admin.getUsername());
    }


    // 修改密码
    public AuthResponse ResetPassword(AdminResetPasswordRequest request) {
        logger.debug("Processing reset the password");
        System.out.println(request.getKey());
        // 使用 Optional 来避免 null 检查
        Administer admin = adminRepository.findByAdminKey(request.getKey())
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));
        // 设置新密码并保存
        // 加密新密码
        String encryptedPassword = passwordEncoder.encode(request.getNewpassword());
        admin.setPassword(encryptedPassword);
        adminRepository.save(admin);
        logger.info("Reset password successfully");
        String token = jwtUtil.generateToken(admin.getUserId(), admin.getEmail());
        return new AuthResponse(token, admin.getUserId(), admin.getUsername());
    }

    // 获取管理员信息
    public AdminResponse GetAdminInfo(AdminDetailsRequest request) {
        logger.debug("Get admin information");
        // 使用 Optional 来避免 null 检查
        Administer admin = adminRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));

        logger.info("Get admin information successfully");
        String token = jwtUtil.generateToken(admin.getUserId(), admin.getEmail());
        return new AdminResponse(admin.getUserId(), admin.getUsername(),admin.getAdminKey(),token);
    }


}
