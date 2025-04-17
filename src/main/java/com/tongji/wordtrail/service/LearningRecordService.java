package com.tongji.wordtrail.service;

import com.tongji.wordtrail.model.LearningRecord;
import com.tongji.wordtrail.model.WordLearningDetail;
import com.tongji.wordtrail.repository.LearningRecordRepository;
import com.tongji.wordtrail.dto.LearningStatsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class LearningRecordService {

    private final LearningRecordRepository learningRecordRepository;

    @Autowired
    public LearningRecordService(LearningRecordRepository learningRecordRepository) {
        this.learningRecordRepository = learningRecordRepository;
    }

    public LearningRecord recordLearningActivity(String userId, String type, List<WordLearningDetail> words) {
        LearningRecord record = new LearningRecord();
        record.setUserId(userId);
        record.setDate(new Date());
        record.setType(type);
        record.setWords(words);
        record.setCount(words.size());

        return learningRecordRepository.save(record);
    }

    public LearningStatsResponse getUserLearningStats(String userId, Date startDate, Date endDate) {
        // 可以使用这些函数转换日期范围，如果它们在您的类中存在
        Date startOfDay = getDayStart(startDate, 0);
        Date endOfDay = getDayEnd(endDate, 0);

        List<LearningRecord> records = learningRecordRepository
                .findByUserIdAndDateBetweenOrderByDateDesc(userId, startOfDay, endOfDay);

        // 统计独立单词ID的数量，而非count总和
        long totalLearnedWords = records.stream()
                .filter(r -> "learn".equals(r.getType()))
                .flatMap(r -> r.getWords().stream())
                .map(WordLearningDetail::getWordId) // 假设WordLearningDetail有getWordId方法
                .distinct()
                .count();

        // 复习单词仍使用原来的方式，或者也可以用相同的逻辑处理
        long totalReviewedWords = records.stream()
                .filter(r -> "review".equals(r.getType()))
                .mapToLong(r -> r.getCount())
                .sum();

        double averageSuccessRate = records.stream()
                .mapToDouble(LearningRecord::getSuccessRate)
                .average()
                .orElse(0.0);

        return LearningStatsResponse.builder()
                .totalLearnedWords(totalLearnedWords)
                .totalReviewedWords(totalReviewedWords)
                .averageSuccessRate(averageSuccessRate)
                .consecutiveDays(getConsecutiveLearningDays(userId))
                .dailyAverageWords((totalLearnedWords + totalReviewedWords) / Math.max(1, getDaysBetween(startDate, endDate)))
                .build();
    }
    public Page<LearningRecord> getLearningHistory(String userId, int page, int size, Date startDate, Date endDate) {
        PageRequest pageRequest = PageRequest.of(page, size);
        if (startDate != null && endDate != null) {
            return learningRecordRepository.findByUserIdAndDateBetweenOrderByDateDesc(userId, startDate, endDate, pageRequest);
        }
        return (Page<LearningRecord>) learningRecordRepository.findByUserIdOrderByDateDesc(userId, pageRequest);
    }

    public int getConsecutiveLearningDays(String userId) {
        Date today = getTodayStart();
        int consecutiveDays = 0;

        while (true) {
            Date dayStart = getDayStart(today, -consecutiveDays);
            Date dayEnd = getDayEnd(today, -consecutiveDays);

            long recordCount = learningRecordRepository.countByUserIdAndDateBetween(userId, dayStart, dayEnd);
            if (recordCount == 0) {
                break;
            }
            consecutiveDays++;
        }

        return consecutiveDays;
    }

    public double getSuccessRate(String userId, Date startDate, Date endDate) {
        List<LearningRecord> records = learningRecordRepository
                .findLearningResultsByUserIdAndDateBetween(userId, startDate, endDate);

        if (records.isEmpty()) {
            return 0.0;
        }

        return records.stream()
                .mapToDouble(LearningRecord::getSuccessRate)
                .average()
                .orElse(0.0);
    }

    public List<LearningRecord> getTodayLearningRecords(String userId) {
        Date todayStart = getTodayStart();
        Date todayEnd = getDayEnd(todayStart, 0);

        return learningRecordRepository
                .findByUserIdAndDateBetweenOrderByDateDesc(userId, todayStart, todayEnd);
    }

    // Helper methods
    private Date getTodayStart() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private Date getDayStart(Date date, int dayOffset) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, dayOffset);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private Date getDayEnd(Date date, int dayOffset) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, dayOffset);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }

    private long getDaysBetween(Date startDate, Date endDate) {
        return (endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24) + 1;
    }
}