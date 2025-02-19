// LearningStatsResponse.java
package com.tongji.wordtrail.dto;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class LearningStatsResponse {
    private long totalLearnedWords;
    private long totalReviewedWords;
    private double averageSuccessRate;
    private int consecutiveDays;
    private long totalLearningTime;
    private long dailyAverageWords;
}