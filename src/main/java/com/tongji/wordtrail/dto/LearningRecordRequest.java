// LearningRecordRequest.java
package com.tongji.wordtrail.dto;

import com.tongji.wordtrail.model.WordLearningDetail;
import lombok.Data;
import java.util.List;

@Data
public class LearningRecordRequest {
    private String type;  // "learn" or "review"
    private List<WordLearningDetail> words;
}