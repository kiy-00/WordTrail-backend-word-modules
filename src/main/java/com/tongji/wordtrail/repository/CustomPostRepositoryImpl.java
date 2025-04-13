package com.tongji.wordtrail.repository;

import com.tongji.wordtrail.model.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CustomPostRepositoryImpl implements CustomPostRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<Post> findRandomPosts(int size) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.sample(size)
        );

        AggregationResults<Post> results = mongoTemplate.aggregate(agg, "posts", Post.class);
        return results.getMappedResults();
    }
}
