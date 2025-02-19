// WordLearningDetail.java
package com.tongji.wordtrail.model;

import org.bson.types.ObjectId;
import lombok.Data;

@Data
public class WordLearningDetail {
    private ObjectId wordId;
    private Boolean result;
    private Integer stage;

    // Constructors
    public WordLearningDetail() {}

    public WordLearningDetail(ObjectId wordId, Boolean result, Integer stage) {
        this.wordId = wordId;
        this.result = result;
        this.stage = stage;
    }
}