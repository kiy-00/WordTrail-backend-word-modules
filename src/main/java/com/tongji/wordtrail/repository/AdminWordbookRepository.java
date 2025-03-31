package com.tongji.wordtrail.repository;

import com.tongji.wordtrail.model.SystemWordbook;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface AdminWordbookRepository extends MongoRepository<SystemWordbook, Long> {
    List<SystemWordbook> findAll();  // 添加方法，获取所有词书
    Optional<SystemWordbook> findById(ObjectId id); // 通过ID获取词书
}
