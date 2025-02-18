package com.tongji.wordtrail.model;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(collection = "learning_records")
public class LearningRecord {
    @Id
    private ObjectId id;

    private ObjectId userId;
    private Date date;
    private String type;    // "learn" 或 "review"
    private int count;      // 学习或复习的单词数量
    private Date createTime;

    public LearningRecord() {
        this.createTime = new Date();
        this.date = new Date();
    }

    public LearningRecord(ObjectId userId, String type, int count) {
        this();
        this.userId = userId;
        this.type = type;
        this.count = count;
    }
}