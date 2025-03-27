package com.tongji.wordtrail.dto;

import lombok.Data;

@Data
public class AdminResponse {
    private String user_id;
    private String username;
    private String key;
    private String token;
    public AdminResponse(String user_id, String username, String key, String token) {
        this.user_id = user_id;
        this.username = username;
        this.key = key;
        this.token = token;
    }
}
