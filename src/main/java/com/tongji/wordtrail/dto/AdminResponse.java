package com.tongji.wordtrail.dto;

import lombok.Data;

@Data
public class AdminResponse {
    private String token;
    private String avatarUrl;
    public AdminResponse(String token, String avatarUrl) {
        this.token = token;
        this.avatarUrl = avatarUrl;
    }
}
