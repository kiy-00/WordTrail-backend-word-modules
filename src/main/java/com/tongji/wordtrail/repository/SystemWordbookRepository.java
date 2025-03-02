package com.tongji.wordtrail.repository;

import com.tongji.wordtrail.model.SystemWordbook;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SystemWordbookRepository extends MongoRepository<SystemWordbook, ObjectId> {
    List<SystemWordbook> findByLanguage(String language);
}