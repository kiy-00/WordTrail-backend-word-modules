package com.tongji.wordtrail.dto;

import lombok.Data;

@Data
public class EmailResponse {
    private String token;
    private String userId;
    private String username;

    public EmailResponse(String token, String userId, String username) {
        this.token = token;
        this.userId = userId;
        this.username = username;
    }
}