package com.tongji.wordtrail.repository;

import com.tongji.wordtrail.model.LearningGoal;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface LearningGoalRepository extends MongoRepository<LearningGoal, ObjectId> {

    // 通过用户ID查找学习目标
    Optional<LearningGoal> findByUserId(String userId);
}