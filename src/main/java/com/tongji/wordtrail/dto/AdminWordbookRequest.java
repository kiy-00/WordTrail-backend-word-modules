package com.tongji.wordtrail.dto;

import lombok.Data;

import java.util.List;
@Data
public class AdminWordbookRequest {
    private String language;
    private String bookName;
    private String description;
    private List<AdminWordRequest> words;

}

