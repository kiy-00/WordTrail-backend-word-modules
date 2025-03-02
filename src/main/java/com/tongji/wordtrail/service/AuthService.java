package com.tongji.wordtrail.service;

import com.tongji.wordtrail.dto.AuthResponse;
import com.tongji.wordtrail.dto.LoginRequest;
import com.tongji.wordtrail.dto.RegisterRequest;
import com.tongji.wordtrail.dto.UserDetailsResponse;
import com.tongji.wordtrail.model.User;  // 确保正确导入 User 类
import com.tongji.wordtrail.repository.UserRepository;
import com.tongji.wordtrail.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        String username = request.getUsername();
        logger.info("Starting registration process. Request details:");
        logger.info("Username: '{}' (length: {})", username, username.length());

        // 打印用户名的每个字符的ASCII值，用于检查是否有不可见字符
        StringBuilder charDetails = new StringBuilder("Username characters: ");
        for (char c : username.toCharArray()) {
            charDetails.append(String.format("[%c: %d] ", c, (int)c));
        }
        logger.info(charDetails.toString());

        // 在 Java 8 中正确处理数据库查询结果
        Optional<User> existingUser = userRepository.findByUsername(username);
        logger.info("Database query completed. User exists: {}", existingUser.isPresent());

        if (existingUser.isPresent()) {
            logger.error("Username already exists: '{}'", username);
            throw new RuntimeException("Username already exists");
        }

        // 创建新用户
        User user = new User();
        String userId = UUID.randomUUID().toString(); // 生成新的UUID
        user.setUserId(userId);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setStatus('1'); // 设置为激活状态
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());

        try {
            logger.info("Attempting to save new user with ID: {}", userId);
            User savedUser = userRepository.save(user);
            logger.info("Successfully saved user to database. Username: {}, ID: {}",
                    savedUser.getUsername(), savedUser.getUserId());

            String token = jwtUtil.generateToken(savedUser.getUserId(), savedUser.getUsername());
            return new AuthResponse(token, savedUser.getUserId(), savedUser.getUsername());
        } catch (Exception e) {
            logger.error("Failed to save user. Error: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create user: " + e.getMessage());
        }
    }

    public AuthResponse login(LoginRequest request) {
        logger.debug("Processing login for username: {}", request.getUsername());

        return userRepository.findByUsername(request.getUsername())
                .map(user -> {
                    if (!user.isActive()) {
                        logger.warn("Account is not active for username: {}", user.getUsername());
                        throw new RuntimeException("User account is not active");
                    }

                    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                        logger.warn("Invalid password for username: {}", user.getUsername());
                        throw new RuntimeException("Invalid credentials");
                    }

                    logger.info("User logged in successfully: {}", user.getUsername());
                    String token = jwtUtil.generateToken(user.getUserId(), user.getUsername());
                    return new AuthResponse(token, user.getUserId(), user.getUsername());
                })
                .orElseThrow(() -> {
                    logger.error("User not found: {}", request.getUsername());
                    return new RuntimeException("User not found");
                });
    }

    /**
     * Retrieves user details by userId
     *
     * @param userId the unique identifier of the user
     * @return UserDetailsResponse containing the user information
     * @throws UsernameNotFoundException if the user with the given id is not found
     */
    public UserDetailsResponse getUserDetails(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

        return UserDetailsResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .active(user.isActive())
                .createTime(user.getCreateTime())
                .updateTime(user.getUpdateTime())
                .build();
    }
}