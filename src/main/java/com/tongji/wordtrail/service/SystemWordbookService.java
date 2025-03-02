package com.tongji.wordtrail.service;

import com.tongji.wordtrail.model.SystemWordbook;
import com.tongji.wordtrail.repository.SystemWordbookRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SystemWordbookService {

    private final MongoTemplate mongoTemplate;
    private final WordService wordService;
    private final SystemWordbookRepository systemWordbookRepository;

    @Autowired
    public SystemWordbookService(MongoTemplate mongoTemplate, WordService wordService, SystemWordbookRepository systemWordbookRepository) {
        this.mongoTemplate = mongoTemplate;
        this.wordService = wordService;
        this.systemWordbookRepository = systemWordbookRepository;
    }

    /**
     * 获取系统词书
     */
    public Optional<Map<String, Object>> getSystemWordbook(String id) {
        try {
            ObjectId objectId = new ObjectId(id);
            return Optional.ofNullable(mongoTemplate.findById(objectId, Document.class, "system_wordbooks"))
                    .map(document -> {
                        Map<String, Object> result = new HashMap<>();

                        // 处理_id字段，转换为字符串
                        result.put("id", document.getObjectId("_id").toString());

                        // 复制其他普通字段
                        result.put("language", document.getString("language"));
                        result.put("bookName", document.getString("bookName"));
                        result.put("description", document.getString("description"));
                        result.put("createUser", document.getString("createUser"));

                        // 处理words字段，确保ObjectId列表被正确转换
                        List<ObjectId> words = (List<ObjectId>) document.get("words");
                        if (words != null) {
                            List<String> wordIds = words.stream()
                                    .map(ObjectId::toString)
                                    .collect(Collectors.toList());
                            result.put("words", wordIds);
                        } else {
                            result.put("words", Collections.emptyList());
                        }

                        return result;
                    });
        } catch (IllegalArgumentException e) {
            log.error("Invalid ObjectId format: {}", id, e);
            return Optional.empty();
        }
    }

    public Page<Map<String, Object>> getSystemWordbooks(
            int page,
            int size,
            Map<String, String> filters) {
        // 移除分页相关的过滤参数
        if (filters != null) {
            filters.remove("page");
            filters.remove("size");
        }

        Query query = new Query().with(PageRequest.of(page, size));

        // 添加过滤条件
        if (filters != null) {
            filters.entrySet().stream()
                    .filter(entry -> StringUtils.hasText(entry.getValue()))
                    .forEach(entry -> query.addCriteria(
                            Criteria.where(entry.getKey()).is(entry.getValue())
                    ));
        }

        long total = mongoTemplate.count(query, "system_wordbooks");
        List<Map<String, Object>> wordbooks = mongoTemplate.find(query, Document.class, "system_wordbooks")
                .stream()
                .map(document -> {
                    Map<String, Object> map = new HashMap<>();

                    map.put("id", document.getObjectId("_id").toString());
                    map.put("language", document.getString("language"));
                    map.put("bookName", document.getString("bookName"));
                    map.put("description", document.getString("description"));
                    map.put("createUser", document.getString("createUser"));

                    // 处理 words 数组，保留完整信息
                    List<ObjectId> words = (List<ObjectId>) document.get("words");
                    map.put("words", words != null
                            ? words.stream().map(wordId -> {
                        Map<String, Object> wordMap = new HashMap<>();
                        wordMap.put("id", wordId.toString());
                        return wordMap;
                    }).collect(Collectors.toList())
                            : Collections.emptyList());

                    return map;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(wordbooks, PageRequest.of(page, size), total);
    }

    /**
     * 创建系统词书
     */
    public Map<String, Object> createSystemWordbook(Map<String, Object> wordbookData) {
        wordbookData.put("createUser", "system");
        Document doc = new Document(wordbookData);
        return Optional.ofNullable(mongoTemplate.save(doc, "system_wordbooks"))
                .map(document -> document.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
                .orElseThrow(() -> new RuntimeException("Failed to create system wordbook"));
    }

    /**
     * 更新系统词书
     */
    public Optional<Map<String, Object>> updateSystemWordbook(String id, Map<String, Object> updateData) {
        Query query = new Query(Criteria.where("_id").is(id));
        Update update = new Update();
        updateData.forEach(update::set);

        // 创建 FindAndModifyOptions 并设置 returnNew 为 true
        FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);

        return Optional.ofNullable(
                        mongoTemplate.findAndModify(query, update, options, Document.class, "system_wordbooks"))
                .map(document -> document.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    /**
     * 删除系统词书
     */
    public boolean deleteSystemWordbook(String id) {
        Query query = new Query(Criteria.where("_id").is(id));
        return mongoTemplate.remove(query, "system_wordbooks").getDeletedCount() > 0;
    }

    /**
     * 向系统词书添加单词
     */
    public Optional<Map<String, Object>> addWordsToWordbook(String wordbookId, List<String> wordIds) {
        log.info("Starting to add words to wordbook. Wordbook ID: {}", wordbookId);
        log.info("Word IDs to add: {}", wordIds);

        if (wordIds == null || wordIds.isEmpty()) {
            log.warn("Word IDs list is null or empty");
            throw new IllegalArgumentException("Word IDs cannot be null or empty");
        }

        // 查询单词是否存在
        List<Map<String, Object>> words = wordService.getWordsByIds(wordIds);
        log.info("Found {} words out of {} requested", words.size(), wordIds.size());

        if (words.size() != wordIds.size()) {
            List<String> foundWordIds = words.stream()
                    .map(word -> word.get("_id").toString())
                    .collect(Collectors.toList());
            List<String> missingWordIds = wordIds.stream()
                    .filter(id -> !foundWordIds.contains(id))
                    .collect(Collectors.toList());

            log.error("Missing words: {}", missingWordIds);
            throw new RuntimeException("Some words do not exist");
        }

        // 将字符串ID转换为ObjectId
        List<ObjectId> objectIds = wordIds.stream()
                .map(ObjectId::new)
                .collect(Collectors.toList());

        log.info("Creating update query for wordbook: {}", wordbookId);
        Query query = new Query(Criteria.where("_id").is(wordbookId));
        Update update = new Update().addToSet("words").each(objectIds.toArray());

        // 设置返回更新后的文档
        FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);

        log.info("Executing update operation");
        return Optional.ofNullable(
                        mongoTemplate.findAndModify(query, update, options, Document.class, "system_wordbooks"))
                .map(document -> {
                    Map<String, Object> result = document.entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                    log.info("Successfully updated wordbook: {}", wordbookId);
                    return result;
                });
    }

    /**
     * 从系统词书移除单词
     */
    public Optional<Map<String, Object>> removeWordsFromWordbook(String wordbookId, List<ObjectId> wordIds) {
        ObjectId bookObjectId = new ObjectId(wordbookId);

        Query query = new Query(Criteria.where("_id").is(bookObjectId));
        Update update = new Update().pullAll("words", wordIds.toArray());

        // 创建 FindAndModifyOptions 并设置 returnNew 为 true
        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true);

        return Optional.ofNullable(
                        mongoTemplate.findAndModify(query, update, options, Document.class, "system_wordbooks"))
                .map(document -> document.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    /**
     * 获取词书中的所有单词
     */
    public List<Map<String, Object>> getWordbookWords(String wordbookId) {
        Optional<Map<String, Object>> wordbook = getSystemWordbook(wordbookId);
        if (wordbook.isPresent()) {
            @SuppressWarnings("unchecked")
            List<String> wordIds = (List<String>) wordbook.get().get("words");
            Map<String, String> queryParams = new HashMap<>();
            queryParams.put("_id", String.join(",", wordIds));
            return wordService.getWords(queryParams);
        }
        return Collections.emptyList();  // 替换 List.of()
    }

    public List<Map<String, Object>> getSystemWordbooksByLanguage(String language) {
        List<SystemWordbook> wordbooks = systemWordbookRepository.findByLanguage(language);
        return wordbooks.stream()
                .map(wordbook -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", wordbook.getId().toString());
                    map.put("language", wordbook.getLanguage());
                    map.put("bookName", wordbook.getBookName());
                    map.put("description", wordbook.getDescription());
                    map.put("createUser", wordbook.getCreateUser());
                    // 不包含单词列表
                    return map;
                })
                .collect(Collectors.toList());
    }
}