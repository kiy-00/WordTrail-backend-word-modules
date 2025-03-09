package com.tongji.wordtrail.service;

import com.mongodb.client.result.DeleteResult;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
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
     * 工具方法：将 Document 转换为 Map，处理 _id 字段并重命名为 id
     */
    private Map<String, Object> convertDocumentToMap(Document document) {
        if (document == null) {
            return null;
        }

        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : document.entrySet()) {
            if (entry.getKey().equals("_id")) {
                // 将 _id 转换为字符串，并重命名为 id
                if (entry.getValue() instanceof ObjectId) {
                    result.put("id", ((ObjectId) entry.getValue()).toString());
                } else {
                    // 如果不是 ObjectId 类型，尝试创建 ObjectId 或直接转字符串
                    try {
                        result.put("id", new ObjectId(entry.getValue().toString()).toString());
                    } catch (Exception e) {
                        result.put("id", entry.getValue().toString());
                    }
                }
            } else {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    /**
     * 工具方法：将 Document 列表转换为 Map 列表，处理 _id 字段并重命名为 id
     */
    private List<Map<String, Object>> convertDocumentsToMaps(List<Document> documents) {
        if (documents == null) {
            return new ArrayList<>();
        }

        return documents.stream()
                .map(this::convertDocumentToMap)
                .collect(Collectors.toList());
    }

    /**
     * 获取单词 - 确保返回的 id 是 ObjectId 的字符串形式
     */
    public Optional<Map<String, Object>> getWord(String id) {
        try {
            // 将字符串 id 转换为 ObjectId
            ObjectId objectId = new ObjectId(id);
            Query query = new Query(Criteria.where("_id").is(objectId));

            Document document = mongoTemplate.findOne(query, Document.class, "words");

            if (document == null) {
                return Optional.empty();
            }

            return Optional.of(convertDocumentToMap(document));
        } catch (IllegalArgumentException e) {
            // 处理无效的 ObjectId 格式
            log.error("Invalid ObjectId format: {}", id, e);
            return Optional.empty();
        }
    }

    /**
     * 批量获取单词 - 确保返回的 id 是 ObjectId 的字符串形式
     */
    public List<Map<String, Object>> getWordsByIds(List<String> ids) {
        try {
            // 将字符串 id 列表转换为 ObjectId 列表
            List<ObjectId> objectIds = ids.stream()
                    .map(ObjectId::new)
                    .collect(Collectors.toList());

            Query query = new Query(Criteria.where("_id").in(objectIds));
            List<Document> documents = mongoTemplate.find(query, Document.class, "words");

            return convertDocumentsToMaps(documents);
        } catch (IllegalArgumentException e) {
            // 处理无效的 ObjectId 格式
            log.error("Invalid ObjectId format in ids list", e);
            return new ArrayList<>();
        }
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

        List<Document> documents = mongoTemplate.find(query, Document.class, "words");
        return convertDocumentsToMaps(documents);
    }

    /**
     * 分页获取单词
     */
    public Page<Map<String, Object>> getWordsWithPagination(int page, int size) {
        Query query = new Query().with(PageRequest.of(page, size));
        long total = mongoTemplate.count(query, "words");

        List<Document> documents = mongoTemplate.find(query, Document.class, "words");
        List<Map<String, Object>> words = convertDocumentsToMaps(documents);

        return new PageImpl<>(words, PageRequest.of(page, size), total);
    }

    /**
     * 保存单词数据
     */
    public Map<String, Object> saveWord(Map<String, Object> wordData) {
        // 如果入参中有 id 字段，将其转换为 _id
        if (wordData.containsKey("id")) {
            Object idValue = wordData.remove("id");
            if (idValue instanceof String) {
                try {
                    wordData.put("_id", new ObjectId((String) idValue));
                } catch (IllegalArgumentException e) {
                    // 如果不是有效的 ObjectId，则忽略
                    log.warn("Invalid ObjectId format for id: {}", idValue);
                }
            } else {
                wordData.put("_id", idValue);
            }
        }

        Document doc = new Document(wordData);
        Document savedDocument = mongoTemplate.save(doc, "words");

        if (savedDocument == null) {
            throw new RuntimeException("Failed to save word");
        }

        return convertDocumentToMap(savedDocument);
    }

    /**
     * 批量保存单词
     */
    public List<Map<String, Object>> saveWords(List<Map<String, Object>> wordsData) {
        List<Document> savedDocuments = wordsData.stream()
                .map(data -> {
                    // 如果有 id 字段，转换为 _id
                    if (data.containsKey("id")) {
                        Object idValue = data.remove("id");
                        if (idValue instanceof String) {
                            try {
                                data.put("_id", new ObjectId((String) idValue));
                            } catch (IllegalArgumentException e) {
                                log.warn("Invalid ObjectId format for id: {}", idValue);
                            }
                        } else {
                            data.put("_id", idValue);
                        }
                    }
                    return new Document(data);
                })
                .map(doc -> mongoTemplate.save(doc, "words"))
                .collect(Collectors.toList());

        return convertDocumentsToMaps(savedDocuments);
    }

    /**
     * 更新单词
     */
    public Optional<Map<String, Object>> updateWord(String id, Map<String, Object> updateData) {
        try {
            ObjectId objectId = new ObjectId(id);
            Query query = new Query(Criteria.where("_id").is(objectId));
            Update update = new Update();

            // 检查是否存在 id 字段，如果有则移除，避免更新主键
            updateData.remove("id");
            updateData.remove("_id");

            updateData.forEach(update::set); // 使用 update.set 方法更新字段

            Document updated = mongoTemplate.findAndModify(query, update, Document.class, "words");
            if (updated == null) {
                return Optional.empty();
            }

            return Optional.of(convertDocumentToMap(updated));
        } catch (IllegalArgumentException e) {
            log.error("Invalid ObjectId format: {}", id, e);
            return Optional.empty();
        }
    }

    /**
     * 删除单词
     */
    public boolean deleteWord(String id) {
        try {
            ObjectId objectId = new ObjectId(id);
            Query query = new Query(Criteria.where("_id").is(objectId));
            DeleteResult result = mongoTemplate.remove(query, "words");
            return result.getDeletedCount() > 0;
        } catch (IllegalArgumentException e) {
            log.error("Invalid ObjectId format: {}", id, e);
            return false;
        }
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

    /**
     * 生成混淆选项
     */
    public List<String> generateConfusionOptions(String wordId) {
        // 1. 获取目标单词
        Map<String, Object> targetWord = getWord(wordId)
                .orElseThrow(() -> new IllegalArgumentException("Word not found"));

        // 2. 构建查询条件来找到相似的单词
        try {
            ObjectId objectId = new ObjectId(wordId);
            Query query = new Query();
            query.addCriteria(Criteria.where("_id").ne(objectId)); // 排除目标单词自身

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
            List<Document> documents = mongoTemplate.find(query, Document.class, "words");
            List<Map<String, Object>> similarWords = convertDocumentsToMaps(documents);

            // 4. 如果找到的单词不够4个，放宽条件重新查询
            if (similarWords.size() < 4) {
                query = new Query();
                query.addCriteria(Criteria.where("_id").ne(objectId));
                if (targetWord.containsKey("difficulty")) {
                    query.addCriteria(Criteria.where("difficulty").is(targetWord.get("difficulty")));
                }
                documents = mongoTemplate.find(query, Document.class, "words");
                similarWords = convertDocumentsToMaps(documents);
            }

            // 5. 随机选择4个单词
            Collections.shuffle(similarWords);
            return similarWords.stream()
                    .limit(4)
                    .map(word -> word.get("id").toString())
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            log.error("Invalid ObjectId format: {}", wordId, e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取词书中熟练度模糊的单词（0.5 <= proficiency < 0.8）
     */
    public List<Map<String, Object>> getFuzzyWordsFromBook(String userId, String bookId) {
        try {
            // 查询学习进度
            Query query = new Query(Criteria.where("userId").is(userId)
                    .and("bookId").is(bookId)
                    .and("proficiency").gte(0.5).lt(0.8));

            List<Document> learningProgressList = mongoTemplate.find(query, Document.class, "word_learning_progress");

            // 提取wordId列表
            List<ObjectId> wordIds = learningProgressList.stream()
                    .map(doc -> doc.get("wordId"))
                    .filter(id -> id instanceof ObjectId)
                    .map(id -> (ObjectId) id)
                    .collect(Collectors.toList());

            if (wordIds.isEmpty()) {
                return new ArrayList<>();
            }

            // 查询单词详情
            Query wordQuery = new Query(Criteria.where("_id").in(wordIds));
            List<Document> wordDocuments = mongoTemplate.find(wordQuery, Document.class, "words");

            return convertDocumentsToMaps(wordDocuments);
        } catch (Exception e) {
            log.error("Error getting fuzzy words from book: ", e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取词书中熟悉的单词（0.8 <= proficiency <= 1）
     */
    public List<Map<String, Object>> getFamiliarWordsFromBook(String userId, String bookId) {
        try {
            // 查询学习进度
            Query query = new Query(Criteria.where("userId").is(userId)
                    .and("bookId").is(bookId)
                    .and("proficiency").gte(0.8).lte(1.0));

            List<Document> learningProgressList = mongoTemplate.find(query, Document.class, "word_learning_progress");

            // 提取wordId列表
            List<ObjectId> wordIds = learningProgressList.stream()
                    .map(doc -> doc.get("wordId"))
                    .filter(id -> id instanceof ObjectId)
                    .map(id -> (ObjectId) id)
                    .collect(Collectors.toList());

            if (wordIds.isEmpty()) {
                return new ArrayList<>();
            }

            // 查询单词详情
            Query wordQuery = new Query(Criteria.where("_id").in(wordIds));
            List<Document> wordDocuments = mongoTemplate.find(wordQuery, Document.class, "words");

            return convertDocumentsToMaps(wordDocuments);
        } catch (Exception e) {
            log.error("Error getting familiar words from book: ", e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取词书中未学习的单词（proficiency = 0）
     */
    public List<Map<String, Object>> getUnlearnedWordsFromBook(String userId, String bookId) {
        try {
            // 查询学习进度
            Query query = new Query(Criteria.where("userId").is(userId)
                    .and("bookId").is(bookId)
                    .and("proficiency").is(0.0));

            List<Document> learningProgressList = mongoTemplate.find(query, Document.class, "word_learning_progress");

            // 提取wordId列表
            List<ObjectId> wordIds = learningProgressList.stream()
                    .map(doc -> doc.get("wordId"))
                    .filter(id -> id instanceof ObjectId)
                    .map(id -> (ObjectId) id)
                    .collect(Collectors.toList());

            if (wordIds.isEmpty()) {
                return new ArrayList<>();
            }

            // 查询单词详情
            Query wordQuery = new Query(Criteria.where("_id").in(wordIds));
            List<Document> wordDocuments = mongoTemplate.find(wordQuery, Document.class, "words");

            return convertDocumentsToMaps(wordDocuments);
        } catch (Exception e) {
            log.error("Error getting unlearned words from book: ", e);
            return new ArrayList<>();
        }
    }
}