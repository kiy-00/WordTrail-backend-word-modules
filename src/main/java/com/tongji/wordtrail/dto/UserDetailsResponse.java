package com.tongji.wordtrail.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailsResponse {
    private String userId;
    private String username;
    private String email;
    private String phone;
    private String avatarUrl;
    private boolean active;
    private Date createTime;
    private Date updateTime;
}