package com.tongji.wordtrail.repository;

import com.tongji.wordtrail.model.SystemWordbook;
import com.tongji.wordtrail.model.Words;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface WordRepository extends MongoRepository<Words, ObjectId> {
    List<Words> findByIdIn(List<ObjectId> ids);
}
