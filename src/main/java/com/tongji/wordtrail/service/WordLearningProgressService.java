package com.tongji.wordtrail.service;

import com.tongji.wordtrail.model.WordLearningProgress;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 单词学习进度服务
 * 管理用户的单词学习进度,包括进度查询、更新和统计等功能
 */
@Service
public class WordLearningProgressService {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public WordLearningProgressService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * 获取用户特定单词的学习进度
     */
    public Optional<WordLearningProgress> getWordProgress(String userId, String wordId) {
        Query query = new Query(Criteria.where("userId").is(new ObjectId(userId))
                .and("wordId").is(new ObjectId(wordId)));
        WordLearningProgress result = mongoTemplate.findOne(query, WordLearningProgress.class);
        return Optional.ofNullable(result);
    }

    /**
     * 获取用户所有低于指定熟练度阈值的单词进度
     */
    public List<WordLearningProgress> getUserWordProgress(String userId, double proficiencyThreshold) {
        Query query = new Query(Criteria.where("userId").is(new ObjectId(userId))
                .and("proficiency").lte(proficiencyThreshold));
        return mongoTemplate.find(query, WordLearningProgress.class);
    }

    /**
     * 更新用户的单词学习进度
     */
    public WordLearningProgress updateWordProgress(String userId, String wordId, double proficiency) {
        Query query = new Query(Criteria.where("userId").is(new ObjectId(userId))
                .and("wordId").is(new ObjectId(wordId)));

        Update update = new Update()
                .set("proficiency", proficiency)
                .set("lastReviewTime", new Date());

        return mongoTemplate.findAndModify(
                query,
                update,
                org.springframework.data.mongodb.core.FindAndModifyOptions.options().upsert(true).returnNew(true),
                WordLearningProgress.class
        );
    }

    /**
     * 获取需要复习的单词列表
     */
    public List<WordLearningProgress> getWordsNeedingReview(String userId,
                                                            double proficiencyThreshold,
                                                            Date lastReviewBefore) {
        Query query = new Query(Criteria.where("userId").is(new ObjectId(userId))
                .and("proficiency").lt(proficiencyThreshold)
                .and("lastReviewTime").lt(lastReviewBefore));

        return mongoTemplate.find(query, WordLearningProgress.class);
    }

    /**
     * 获取用户已掌握的单词列表
     */
    public List<WordLearningProgress> getMasteredWords(String userId, double masteryThreshold) {
        Query query = new Query(Criteria.where("userId").is(new ObjectId(userId))
                .and("proficiency").gte(masteryThreshold));
        return mongoTemplate.find(query, WordLearningProgress.class);
    }

    /**
     * 计算用户的平均熟练度
     */
    public double calculateAverageProficiency(String userId) {
        Query query = new Query(Criteria.where("userId").is(new ObjectId(userId)));
        List<WordLearningProgress> progressList = mongoTemplate.find(query, WordLearningProgress.class);

        if (progressList.isEmpty()) {
            return 0.0;
        }

        double totalProficiency = progressList.stream()
                .mapToDouble(WordLearningProgress::getProficiency)
                .sum();

        return totalProficiency / progressList.size();
    }

    /**
     * 批量获取单词的学习进度
     */
    public List<WordLearningProgress> getProgressForWords(String userId, List<String> wordIds) {
        List<ObjectId> objectIds = wordIds.stream()
                .map(ObjectId::new)
                .collect(Collectors.toList());  // 替换 toList()

        Query query = new Query(Criteria.where("userId").is(new ObjectId(userId))
                .and("wordId").in(objectIds));
        return mongoTemplate.find(query, WordLearningProgress.class);
    }
}