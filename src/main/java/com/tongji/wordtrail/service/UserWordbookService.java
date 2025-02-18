package com.tongji.wordtrail.service;

import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
public class UserWordbookService {

    private final MongoTemplate mongoTemplate;
    private final WordService wordService;

    @Autowired
    public UserWordbookService(MongoTemplate mongoTemplate, WordService wordService) {
        this.mongoTemplate = mongoTemplate;
        this.wordService = wordService;
    }

    /**
     * 获取用户词书
     */
    public Optional<Map<String, Object>> getUserWordbook(String id) {
        return Optional.ofNullable(mongoTemplate.findById(id, Document.class, "user_wordbooks"))
                .map(document -> document.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    /**
     * 分页获取用户的词书列表
     */
    public Page<Map<String, Object>> getUserWordbooks(
            String userId,
            int page,
            int size,
            Map<String, String> filters) {
        Query query = new Query(Criteria.where("createUser").is(userId))
                .with(PageRequest.of(page, size));

        // 添加其他过滤条件
        filters.entrySet().stream()
                .filter(entry -> StringUtils.hasText(entry.getValue()))
                .forEach(entry -> query.addCriteria(
                        Criteria.where(entry.getKey()).is(entry.getValue())
                ));

        long total = mongoTemplate.count(query, "user_wordbooks");
        List<Map<String, Object>> wordbooks = mongoTemplate.find(query, Document.class, "user_wordbooks")
                .stream()
                .map(document -> document.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
                .collect(Collectors.toList());

        return new PageImpl<>(wordbooks, PageRequest.of(page, size), total);
    }

    /**
     * 创建用户词书
     */
    public Map<String, Object> createUserWordbook(String userId, Map<String, Object> wordbookData) {
        // 设置初始属性
        wordbookData.put("createUser", userId);
        wordbookData.put("createTime", new Date());
        wordbookData.put("isPublic", wordbookData.getOrDefault("isPublic", false));
        wordbookData.put("status", "pending");
        wordbookData.put("words", wordbookData.getOrDefault("words", Collections.emptyList()));

        // 验证标签数量
        @SuppressWarnings("unchecked")
        List<String> tags = (List<String>) wordbookData.getOrDefault("tags", Collections.emptyList());
        if (tags.size() > 5) {
            throw new IllegalArgumentException("Cannot have more than 5 tags");
        }

        Document doc = new Document(wordbookData);
        return Optional.ofNullable(mongoTemplate.save(doc, "user_wordbooks"))
                .map(document -> document.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
                .orElseThrow(() -> new RuntimeException("Failed to create user wordbook"));
    }

    /**
     * 更新用户词书
     */
    public Optional<Map<String, Object>> updateUserWordbook(String id, String userId, Map<String, Object> updateData) {
        // 验证所有权
        Query query = new Query(Criteria.where("_id").is(id).and("createUser").is(userId));
        if (!mongoTemplate.exists(query, "user_wordbooks")) {
            return Optional.empty();
        }

        Update update = new Update();
        updateData.forEach((key, value) -> {
            if (!key.equals("createUser") && !key.equals("createTime") && !key.equals("status")) {
                update.set(key, value);
            }
        });

        FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);

        return Optional.ofNullable(
                        mongoTemplate.findAndModify(query, update, options, Document.class, "user_wordbooks"))
                .map(document -> document.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    /**
     * 删除用户词书
     */
    public boolean deleteUserWordbook(String id, String userId) {
        Query query = new Query(Criteria.where("_id").is(id).and("createUser").is(userId));
        return mongoTemplate.remove(query, "user_wordbooks").getDeletedCount() > 0;
    }

    /**
     * 更新词书审核状态（管理员操作）
     */
    public Optional<Map<String, Object>> updateWordbookStatus(String id, String status) {
        if (!Arrays.asList("pending", "approved", "rejected").contains(status)) {
            throw new IllegalArgumentException("Invalid status");
        }

        Query query = new Query(Criteria.where("_id").is(id));
        Update update = new Update().set("status", status);

        // 创建选项对象，设置返回更新后的文档
        FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);

        return Optional.ofNullable(
                        mongoTemplate.findAndModify(query, update, options, Document.class, "user_wordbooks"))
                .map(document -> document.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    /**
     * 获取已审核的公开词书
     */
    public Page<Map<String, Object>> getPublicWordbooks(int page, int size) {
        Query query = new Query(Criteria.where("isPublic").is(true)
                .and("status").is("approved"))
                .with(PageRequest.of(page, size));

        long total = mongoTemplate.count(query, "user_wordbooks");
        List<Map<String, Object>> wordbooks = mongoTemplate.find(query, Document.class, "user_wordbooks")
                .stream()
                .map(document -> document.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
                .collect(Collectors.toList());

        return new PageImpl<>(wordbooks, PageRequest.of(page, size), total);
    }

    /**
     * 添加单词到词书
     */
    public Optional<Map<String, Object>> addWordsToWordbook(String id, String userId, List<String> wordIds) {
        // 验证所有单词是否存在
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("_id", String.join(",", wordIds));
        List<Map<String, Object>> words = wordService.getWords(queryParams);

        if (words.size() != wordIds.size()) {
            throw new RuntimeException("Some words do not exist");
        }

        Query query = new Query(Criteria.where("_id").is(id).and("createUser").is(userId));
        Update update = new Update().addToSet("words").each(wordIds.toArray());

        return Optional.ofNullable(
                        mongoTemplate.findAndModify(query, update, Document.class, "user_wordbooks"))
                .map(document -> document.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    /**
     * 从词书中移除单词
     */
    public Optional<Map<String, Object>> removeWordsFromWordbook(String id, String userId, List<String> wordIds) {
        Query query = new Query(Criteria.where("_id").is(id).and("createUser").is(userId));
        Update update = new Update().pullAll("words", wordIds.toArray());

        return Optional.ofNullable(
                        mongoTemplate.findAndModify(query, update, Document.class, "user_wordbooks"))
                .map(document -> document.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    /**
     * 获取词书中的所有单词
     */
    public List<Map<String, Object>> getWordbookWords(String id, String userId) {
        Query query = new Query(Criteria.where("_id").is(id));
        if (userId != null) {
            query.addCriteria(Criteria.where("createUser").is(userId));
        }

        Optional<Map<String, Object>> wordbook = Optional.ofNullable(
                        mongoTemplate.findOne(query, Document.class, "user_wordbooks"))
                .map(document -> document.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

        if (wordbook.isPresent()) {
            @SuppressWarnings("unchecked")
            List<String> wordIds = (List<String>) wordbook.get().get("words");
            Map<String, String> queryParams = new HashMap<>();
            queryParams.put("_id", String.join(",", wordIds));
            return wordService.getWords(queryParams);
        }
        return Collections.emptyList();
    }

    /**
     * 搜索词书
     * @param keyword 搜索关键词 (搜索书名和描述)
     * @param tags 标签列表
     * @param onlyPublic 是否只搜索公开词书
     * @param status 词书状态过滤 (可选)
     * @param page 页码
     * @param size 每页大小
     * @return 分页的词书列表
     */
    public Page<Map<String, Object>> searchWordbooks(
            String keyword,
            List<String> tags,
            boolean onlyPublic,
            String status,
            int page,
            int size) {

        List<Criteria> criteriaList = new ArrayList<>();

        // 关键词搜索
        if (StringUtils.hasText(keyword)) {
            Criteria keywordCriteria = new Criteria().orOperator(
                    Criteria.where("bookName").regex(keyword, "i"),
                    Criteria.where("description").regex(keyword, "i")
            );
            criteriaList.add(keywordCriteria);
        }

        // 标签过滤
        if (tags != null && !tags.isEmpty()) {
            criteriaList.add(Criteria.where("tags").in(tags));
        }

        // 公开状态过滤
        if (onlyPublic) {
            criteriaList.add(Criteria.where("isPublic").is(true));
            criteriaList.add(Criteria.where("status").is("approved"));
        }

        // 状态过滤
        if (StringUtils.hasText(status)) {
            criteriaList.add(Criteria.where("status").is(status));
        }

        // 构建查询
        Query query = new Query();
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        // 添加分页
        PageRequest pageRequest = PageRequest.of(page, size);
        query.with(pageRequest);

        // 执行查询
        long total = mongoTemplate.count(query, "user_wordbooks");
        List<Map<String, Object>> wordbooks = mongoTemplate.find(query, Document.class, "user_wordbooks")
                .stream()
                .map(document -> document.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
                .collect(Collectors.toList());

        return new PageImpl<>(wordbooks, pageRequest, total);
    }

    /**
     * 高级搜索词书
     * @param searchParams 搜索参数
     * @param page 页码
     * @param size 每页大小
     * @return 分页的词书列表
     */
    public Page<Map<String, Object>> advancedSearch(
            Map<String, Object> searchParams,
            int page,
            int size) {

        List<Criteria> criteriaList = new ArrayList<>();

        // 处理基本搜索参数
        if (searchParams.containsKey("keyword")) {
            String keyword = (String) searchParams.get("keyword");
            if (StringUtils.hasText(keyword)) {
                Criteria keywordCriteria = new Criteria().orOperator(
                        Criteria.where("bookName").regex(keyword, "i"),
                        Criteria.where("description").regex(keyword, "i")
                );
                criteriaList.add(keywordCriteria);
            }
        }

        // 处理标签
        if (searchParams.containsKey("tags")) {
            @SuppressWarnings("unchecked")
            List<String> tags = (List<String>) searchParams.get("tags");
            if (tags != null && !tags.isEmpty()) {
                criteriaList.add(Criteria.where("tags").in(tags));
            }
        }

        // 处理语言
        if (searchParams.containsKey("language")) {
            String language = (String) searchParams.get("language");
            if (StringUtils.hasText(language)) {
                criteriaList.add(Criteria.where("language").is(language));
            }
        }

        // 处理创建时间范围
        if (searchParams.containsKey("createTimeStart") || searchParams.containsKey("createTimeEnd")) {
            Criteria dateRangeCriteria = Criteria.where("createTime");
            if (searchParams.containsKey("createTimeStart")) {
                dateRangeCriteria.gte(searchParams.get("createTimeStart"));
            }
            if (searchParams.containsKey("createTimeEnd")) {
                dateRangeCriteria.lte(searchParams.get("createTimeEnd"));
            }
            criteriaList.add(dateRangeCriteria);
        }

        // 处理公开状态
        if (searchParams.containsKey("isPublic")) {
            boolean isPublic = (boolean) searchParams.get("isPublic");
            criteriaList.add(Criteria.where("isPublic").is(isPublic));
            if (isPublic) {
                criteriaList.add(Criteria.where("status").is("approved"));
            }
        }

        // 处理状态
        if (searchParams.containsKey("status")) {
            String status = (String) searchParams.get("status");
            if (StringUtils.hasText(status)) {
                criteriaList.add(Criteria.where("status").is(status));
            }
        }

        // 构建查询
        Query query = new Query();
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        // 添加排序
        if (searchParams.containsKey("sortBy")) {
            String sortBy = (String) searchParams.get("sortBy");
            String sortDirection = (String) searchParams.getOrDefault("sortDirection", "desc");
            Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ?
                    Sort.Direction.ASC : Sort.Direction.DESC;
            query.with(Sort.by(direction, sortBy));
        } else {
            query.with(Sort.by(Sort.Direction.DESC, "createTime"));
        }

        // 添加分页
        PageRequest pageRequest = PageRequest.of(page, size);
        query.with(pageRequest);

        // 执行查询
        long total = mongoTemplate.count(query, "user_wordbooks");
        List<Map<String, Object>> wordbooks = mongoTemplate.find(query, Document.class, "user_wordbooks")
                .stream()
                .map(document -> document.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
                .collect(Collectors.toList());

        return new PageImpl<>(wordbooks, pageRequest, total);
    }
}