package com.tongji.wordtrail.dto;

import lombok.Data;

@Data
public class AdminResponse {
    private String user_id;
    private String username;
    private String key;
    private String email;
    public AdminResponse(String user_id, String username, String key, String email) {
        this.user_id = user_id;
        this.username = username;
        this.key = key;
        this.email = email;
    }
}
