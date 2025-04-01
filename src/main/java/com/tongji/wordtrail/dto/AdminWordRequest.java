package com.tongji.wordtrail.dto;

import lombok.Data;

@Data
public class AdminWordRequest {
    private String word;
    private String meaning;
    private String phonetic;
    private String example;
}
