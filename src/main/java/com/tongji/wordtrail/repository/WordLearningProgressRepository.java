package com.tongji.wordtrail.repository;

import com.tongji.wordtrail.model.WordLearningProgress;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.Date;
import java.util.List;

public interface WordLearningProgressRepository extends MongoRepository<WordLearningProgress, ObjectId> {

    // 查找用户的所有学习进度
    List<WordLearningProgress> findByUserId(ObjectId userId);

    // 查找用户特定单词的学习进度
    WordLearningProgress findByUserIdAndWordId(ObjectId userId, ObjectId wordId);

    // 查找需要复习的单词
    @Query("{'userId': ?0, 'proficiency': {$lt: ?1}, 'lastReviewTime': {$lt: ?2}}")
    List<WordLearningProgress> findWordsNeedingReview(ObjectId userId, double proficiencyThreshold, Date lastReviewBefore);

    // 查找已掌握的单词
    List<WordLearningProgress> findByUserIdAndProficiencyGreaterThanEqual(ObjectId userId, double masteryThreshold);

    // 查找低于指定熟练度的单词
    List<WordLearningProgress> findByUserIdAndProficiencyLessThanEqual(ObjectId userId, double proficiencyThreshold);
}