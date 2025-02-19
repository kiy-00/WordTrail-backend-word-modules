package com.tongji.wordtrail.service;

import com.mongodb.client.result.DeleteResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import org.springframework.data.mongodb.core.query.Query;
import org.bson.Document;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;


@Service
@Slf4j
public class WordService {
    /**
     * 将 MongoTemplate 实例注入到 WordService 类中，以便在类中使用 MongoDB 的操作功能
     */
    private final MongoTemplate mongoTemplate;

    @Autowired
    public WordService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * 获取单词
     */
    public Optional<Map<String, Object>> getWord(String id) {
        return Optional.ofNullable(mongoTemplate.findById(id, Document.class, "words"))
                .map(document -> document.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    public List<Map<String, Object>> getWordsByIds(List<String> ids) {
        Query query = new Query(Criteria.where("_id").in(ids));
        return mongoTemplate.find(query, Document.class, "words")
                .stream()
                .map(document -> document.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
                .collect(Collectors.toList());
    }

    /**
     * 批量获取单词
     */
    public List<Map<String, Object>> getWords(Map<String, String> queryParams) {
        Query query = new Query();

        // 使用 Stream 处理查询参数
        queryParams.entrySet().stream()
                .filter(entry -> StringUtils.hasText(entry.getValue()))
                .forEach(entry -> query.addCriteria(
                        Criteria.where(entry.getKey()).is(entry.getValue())
                ));

        return mongoTemplate.find(query, Document.class, "words").stream()
                .map(document -> document.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
                .collect(Collectors.toList());
    }

    /**
     * 分页获取单词
     */
    public Page<Map<String, Object>> getWordsWithPagination(int page, int size) {
        Query query = new Query().with(PageRequest.of(page, size));
        long total = mongoTemplate.count(query, "words");

        List<Map<String, Object>> words = mongoTemplate.find(query, Document.class, "words").stream()
                .map(document -> document.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
                .collect(Collectors.toList());

        return new PageImpl<>(words, PageRequest.of(page, size), total);
    }
    /**
     * 保存单词数据
     */
    public Map<String, Object> saveWord(Map<String, Object> wordData) {
        Document doc = new Document(wordData);
        return Optional.ofNullable(mongoTemplate.save(doc, "words"))
                .map(document -> document.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
                .orElseThrow(() -> new RuntimeException("Failed to save word"));
    }
    /**
     * 批量保存单词
     */
    public List<Map<String, Object>> saveWords(List<Map<String, Object>> wordsData) {
        return wordsData.stream()
                .map(data -> new Document(data))
                .map(doc -> mongoTemplate.save(doc, "words"))
                .map(document -> document.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
                .collect(Collectors.toList());
    }

    /**
     * 更新单词
     */
    public Optional<Map<String, Object>> updateWord(String id, Map<String, Object> updateData) {
        Query query = new Query(Criteria.where("_id").is(id));
        Update update = new Update();

        updateData.forEach(update::set); // 使用 update.set 方法更新字段

        return Optional.ofNullable(
                        mongoTemplate.findAndModify(query, update, Document.class, "words"))
                .map(document -> document.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    /**
     * 删除单词
     */
    public boolean deleteWord(String id) {
        Query query = new Query(Criteria.where("_id").is(id));
        DeleteResult result = mongoTemplate.remove(query, "words");
        return result.getDeletedCount() > 0;
    }

    /**
     * 聚合查询：按指定字段分组统计
     * @param field 要统计的字段名
     * @return 分组统计结果 Map
     */
    public Map<String, Long> groupByField(String field) {
        log.info("Starting groupByField operation for field: {}", field);

        try {
            // 验证字段名是否有效
            if (!isValidField(field)) {
                log.error("Invalid field name: {}", field);
                throw new IllegalArgumentException("Invalid field name: " + field);
            }

            // 构建聚合管道
            Aggregation agg = Aggregation.newAggregation(
                    Aggregation.group("$" + field).count().as("count")
            );
            log.debug("Executing aggregation: {}", agg);

            // 执行聚合查询
            AggregationResults<Document> results = mongoTemplate.aggregate(
                    agg,
                    "words",
                    Document.class
            );

            // 检查结果
            if (results == null) {
                log.error("Aggregation returned null results for field: {}", field);
                throw new RuntimeException("Aggregation returned null results");
            }

            // 转换结果 - 安全地处理数值类型转换
            Map<String, Long> resultMap = new HashMap<>();
            for (Document doc : results.getMappedResults()) {
                log.debug("Processing document: {}", doc);

                String key = doc.getString("_id");
                if (key == null) {
                    log.warn("Found null key in document: {}", doc);
                    key = "undefined";
                }

                // 安全地获取 count 值，处理 Integer 到 Long 的转换
                Long count;
                Object countObj = doc.get("count");
                if (countObj == null) {
                    log.warn("Found null count in document: {}", doc);
                    count = 0L;
                } else if (countObj instanceof Integer) {
                    count = ((Integer) countObj).longValue();
                } else if (countObj instanceof Long) {
                    count = (Long) countObj;
                } else {
                    log.warn("Unexpected count type: {} in document: {}", countObj.getClass(), doc);
                    count = Long.valueOf(countObj.toString());
                }

                // 处理重复键
                if (resultMap.containsKey(key)) {
                    Long existingCount = resultMap.get(key);
                    log.warn("Duplicate key found, using sum: {} + {}", existingCount, count);
                    resultMap.put(key, existingCount + count);
                } else {
                    resultMap.put(key, count);
                }
            }

            log.info("Successfully completed groupByField for {}, found {} groups", field, resultMap.size());
            log.debug("Group results: {}", resultMap);
            return resultMap;

        } catch (Exception e) {
            log.error("Error in groupByField operation for field: {}", field, e);
            throw new RuntimeException("Failed to perform group by operation", e);
        }
    }

    /**
     * 验证字段名是否有效
     */
    private boolean isValidField(String field) {
        // Java 8 方式创建不可变Set
        Set<String> validFields = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
                "level", "language", "partOfSpeech", "tags"
        )));
        boolean isValid = validFields.contains(field);
        if (!isValid) {
            log.warn("Attempted to group by invalid field: {}", field);
        }
        return isValid;
    }

    public List<String> generateConfusionOptions(String wordId) {
        // 1. 获取目标单词
        Map<String, Object> targetWord = getWord(wordId)
                .orElseThrow(() -> new IllegalArgumentException("Word not found"));

        // 2. 构建查询条件来找到相似的单词
        // 可以基于以下几个特征来构建相似性：
        // - 单词长度相近（比如相差不超过2个字母）
        // - 相同词性
        // - 相似的难度级别
        // - 相同的词根词缀
        // - 相似的音标
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").ne(wordId)); // 排除目标单词自身

        // 添加相似性条件
        if (targetWord.containsKey("length")) {
            int length = (int) targetWord.get("length");
            query.addCriteria(Criteria.where("length")
                    .gte(length - 2)
                    .lte(length + 2));
        }
        if (targetWord.containsKey("pos")) {
            query.addCriteria(Criteria.where("pos").is(targetWord.get("pos")));
        }
        if (targetWord.containsKey("difficulty")) {
            query.addCriteria(Criteria.where("difficulty").is(targetWord.get("difficulty")));
        }

        // 3. 随机获取4个符合条件的单词
        List<Map> similarWords = mongoTemplate.find(query, Map.class, "words");

        // 4. 如果找到的单词不够4个，放宽条件重新查询
        if (similarWords.size() < 4) {
            query = new Query();
            query.addCriteria(Criteria.where("_id").ne(wordId));
            if (targetWord.containsKey("difficulty")) {
                query.addCriteria(Criteria.where("difficulty").is(targetWord.get("difficulty")));
            }
            similarWords = mongoTemplate.find(query, Map.class, "words");
        }

        // 5. 随机选择4个单词
        Collections.shuffle(similarWords);
        return similarWords.stream()
                .limit(4)
                .map(word -> word.get("_id").toString())
                .collect(Collectors.toList());
    }
}