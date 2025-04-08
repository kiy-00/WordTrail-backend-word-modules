package com.tongji.wordtrail.dto;
import lombok.Data;

// 用于获取词汇列表
@Data
public class AdminWordbooksResponse {
    private String id;
    private String bookName;
    private String language;
    private String description;
    private int wordCount;
    public AdminWordbooksResponse() {}
    public AdminWordbooksResponse(String id, String bookName, String language, String description, int wordCount) {
        this.id = id;
        this.bookName = bookName;
        this.language = language;
        this.description = description;
        this.wordCount = wordCount;
    }

}
