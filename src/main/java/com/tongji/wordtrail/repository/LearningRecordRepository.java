package com.tongji.wordtrail.repository;

import com.tongji.wordtrail.model.LearningRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface LearningRecordRepository extends MongoRepository<LearningRecord, String> {  // 改为String

    List<LearningRecord> findByUserIdAndDateBetweenOrderByDateDesc(
            String userId, Date startDate, Date endDate);

    Page<LearningRecord> findByUserIdAndDateBetweenOrderByDateDesc(
            String userId, Date startDate, Date endDate, Pageable pageable);

    @Query(value = "{'userId': ?0, 'date': {'$gte': ?1, '$lte': ?2}}",
            count = true)
    long countByUserIdAndDateBetween(String userId, Date startDate, Date endDate);

    Page<LearningRecord> findByUserIdOrderByDateDesc(String userId, Pageable pageable);

    @Query(value = "{'userId': ?0, 'date': {'$gte': ?1, '$lte': ?2}}",
            fields = "{'words.result': 1}")
    List<LearningRecord> findLearningResultsByUserIdAndDateBetween(
            String userId, Date startDate, Date endDate);
}