package com.tongji.wordtrail.model;

import javax.persistence.*;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;


@Document(collection = "words")
public class Words {
    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    private ObjectId id;

    private List<String> antonyms;

    private int difficulty;

    private String language;

    private List<String> partOfSpeechList;

    private List<String> synonyms;

    private List<String> tags;

    private String word;

    private List<String> phonetics;

    public ObjectId getId() {
        return id;
    }
    public void setId(ObjectId id) {
        this.id = id;
    }
    public List<String> getAntonyms() {
        return antonyms;
    }
    public void setAntonyms(List<String> antonyms) {
        this.antonyms = antonyms;
    }
    public int getDifficulty() {
        return difficulty;
    }
    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }
    public String getLanguage() {
        return language;
    }
    public void setLanguage(String language) {
        this.language = language;
    }
    public List<String> getPartOfSpeechList() {
        return partOfSpeechList;
    }
    public void setPartOfSpeechList(List<String> partOfSpeechList) {
        this.partOfSpeechList = partOfSpeechList;
    }
    public List<String> getSynonyms() {
        return synonyms;
    }
    public void setSynonyms(List<String> synonyms) {
        this.synonyms = synonyms;
    }
    public List<String> getTags() {
        return tags;
    }
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    public String getWord() {
        return word;
    }
    public void setWord(String word) {
        this.word = word;
    }



}
