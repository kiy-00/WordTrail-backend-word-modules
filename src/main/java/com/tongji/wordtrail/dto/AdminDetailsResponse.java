package com.tongji.wordtrail.dto;

import lombok.Data;

@Data
public class AdminDetailsResponse {
    private String user_id;
    private String username;
    private String email;
    private String avatarUrl;
    private String admin_key;
    public AdminDetailsResponse(String user_id, String username, String email, String avatarUrl, String admin_key) {
        this.user_id = user_id;
        this.username = username;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.admin_key = admin_key;
    }
}
