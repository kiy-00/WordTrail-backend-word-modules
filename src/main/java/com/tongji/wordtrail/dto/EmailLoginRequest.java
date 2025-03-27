package com.tongji.wordtrail.dto;

import lombok.Data;

@Data
public class EmailLoginRequest {
    private String email;
    private String password;
}
