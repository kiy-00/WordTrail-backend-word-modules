package com.tongji.wordtrail.dto;
import lombok.Data;

// 用于获取词汇列表
@Data
public class AdminWordbooksResponse {
    private String bookName;
    private String language;
    private String description;
    private int word;
    public AdminWordbooksResponse(String bookName, String language, String description, int word) {
        this.bookName = bookName;
        this.language = language;
        this.description = description;
        this.word = word;
    }

}
