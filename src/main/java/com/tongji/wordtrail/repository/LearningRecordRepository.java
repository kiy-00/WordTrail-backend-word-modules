package com.tongji.wordtrail.repository;

import com.tongji.wordtrail.model.LearningRecord;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Date;
import java.util.List;

public interface LearningRecordRepository extends MongoRepository<LearningRecord, ObjectId> {
    List<LearningRecord> findByUserIdOrderByDateDesc(ObjectId userId);

    @Query("{'userId': ?0, 'date': {'$gte': ?1, '$lte': ?2}}")
    List<LearningRecord> findByUserIdAndDateBetween(ObjectId userId, Date startDate, Date endDate);

    @Query("{'userId': ?0, 'date': ?1}")
    List<LearningRecord> findByUserIdAndDate(ObjectId userId, Date date);

    @Query(value = "{'userId': ?0}", count = true)
    long countByUserId(ObjectId userId);
}