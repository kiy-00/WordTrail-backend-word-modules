package com.tongji.wordtrail.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Document(collection = "learning_records")
public class LearningRecord {
    @Id
    private String id;  // 改为String类型

    private String userId;  // 改为String类型，存储UUID
    private Date date;
    private String type;  // "learn" or "review"
    private Integer count;
    private List<WordLearningDetail> words;

    // Constructors
    public LearningRecord() {}

    public LearningRecord(String userId, Date date, String type, Integer count, List<WordLearningDetail> words) {
        this.userId = userId;
        this.date = date;
        this.type = type;
        this.count = count;
        this.words = words;
    }

    // Helper methods
    public void addWordDetail(WordLearningDetail detail) {
        this.words.add(detail);
        this.count = this.words.size();
    }

    public boolean isSuccessful() {
        if (words == null || words.isEmpty()) {
            return false;
        }
        return words.stream().allMatch(word -> word.getResult());
    }

    public double getSuccessRate() {
        if (words == null || words.isEmpty()) {
            return 0.0;
        }
        long successCount = words.stream().filter(word -> word.getResult()).count();
        return (double) successCount / words.size();
    }
}