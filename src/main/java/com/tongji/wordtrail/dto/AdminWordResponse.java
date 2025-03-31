package com.tongji.wordtrail.dto;

import org.bson.types.ObjectId;

import java.util.List;

public class AdminWordResponse {
    private ObjectId id;
    private String word;
    private List<String> antonyms;
    private String language;
    private List<String> partOfSpeechList;
    private List<String> synonyms;
    private List<String> tags;
    private List<String> phonetics;
    public AdminWordResponse(String word, List<String> antonyms, String language) {
        this.word = word;
        this.antonyms = antonyms;
        this.language = language;
    }
}
