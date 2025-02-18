package com.tongji.wordtrail.service;

import com.tongji.wordtrail.model.LearningRecord;
import com.tongji.wordtrail.repository.LearningRecordRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class LearningRecordService {

    private final LearningRecordRepository learningRecordRepository;
    private final MongoTemplate mongoTemplate;

    @Autowired
    public LearningRecordService(LearningRecordRepository learningRecordRepository, MongoTemplate mongoTemplate) {
        this.learningRecordRepository = learningRecordRepository;
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * 记录学习活动
     */
    public LearningRecord recordLearningActivity(String userId, String type, int count) {
        LearningRecord record = new LearningRecord(new ObjectId(userId), type, count);
        return learningRecordRepository.save(record);
    }

    /**
     * 获取用户的学习统计
     */
    public Map<String, Object> getUserLearningStats(String userId, Date startDate, Date endDate) {
        List<LearningRecord> records = learningRecordRepository.findByUserIdAndDateBetween(
                new ObjectId(userId), startDate, endDate);

        Map<String, Object> stats = new HashMap<>();

        // 总学习次数
        stats.put("totalSessions", records.size());

        // 总学习单词数
        int totalWords = records.stream().mapToInt(LearningRecord::getCount).sum();
        stats.put("totalWords", totalWords);

        // 按类型统计
        Map<String, Integer> typeStats = records.stream()
                .collect(Collectors.groupingBy(
                        LearningRecord::getType,
                        Collectors.summingInt(LearningRecord::getCount)
                ));
        stats.put("byType", typeStats);

        // 日均学习单词数
        long days = (endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24) + 1;
        stats.put("averageWordsPerDay", totalWords / (double) days);

        // 学习天数
        long uniqueDays = records.stream()
                .map(record -> record.getDate().getTime() / (1000 * 60 * 60 * 24))
                .distinct()
                .count();
        stats.put("activeDays", uniqueDays);

        return stats;
    }

    /**
     * 获取学习历史
     */
    public Page<LearningRecord> getLearningHistory(String userId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"));
        Query query = new Query(Criteria.where("userId").is(new ObjectId(userId)))
                .with(pageRequest);

        long total = mongoTemplate.count(query, LearningRecord.class);
        List<LearningRecord> records = mongoTemplate.find(query, LearningRecord.class);

        return new org.springframework.data.domain.PageImpl<>(records, pageRequest, total);
    }

    /**
     * 获取连续学习天数
     */
    public int getConsecutiveLearningDays(String userId) {
        List<LearningRecord> records = learningRecordRepository.findByUserIdOrderByDateDesc(new ObjectId(userId));
        if (records.isEmpty()) {
            return 0;
        }

        // 获取最近的学习日期
        Calendar lastDay = Calendar.getInstance();
        lastDay.setTime(records.get(0).getDate());
        lastDay.set(Calendar.HOUR_OF_DAY, 0);
        lastDay.set(Calendar.MINUTE, 0);
        lastDay.set(Calendar.SECOND, 0);
        lastDay.set(Calendar.MILLISECOND, 0);

        // 计算连续天数
        Set<Long> learningDays = records.stream()
                .map(record -> record.getDate().getTime() / (1000 * 60 * 60 * 24))
                .collect(Collectors.toSet());

        int consecutiveDays = 0;
        while (learningDays.contains(lastDay.getTimeInMillis() / (1000 * 60 * 60 * 24))) {
            consecutiveDays++;
            lastDay.add(Calendar.DAY_OF_MONTH, -1);
        }

        return consecutiveDays;
    }
}