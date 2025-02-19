package com.tongji.wordtrail.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.bson.types.ObjectId;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

// 首先定义复习历史记录的内部类
public class ReviewHistoryEntry {
    private Date time;
    private int stage;
    private boolean result;

    // Constructors
    public ReviewHistoryEntry() {}

    public ReviewHistoryEntry(Date time, int stage, boolean result) {
        this.time = time;
        this.stage = stage;
        this.result = result;
    }

    // Getters and Setters
    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public int getStage() {
        return stage;
    }

    public void setStage(int stage) {
        this.stage = stage;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }
}