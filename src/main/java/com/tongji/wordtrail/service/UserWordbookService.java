package com.tongji.wordtrail.service;

import com.tongji.wordtrail.model.UserWordbook;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;
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
     * 工具方法：将 Document 转换为 Map，将 _id 转换为 id 字符串
     */
    private Map<String, Object> convertDocumentToMap(Document document) {
        if (document == null) {
            return null;
        }

        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : document.entrySet()) {
            if (entry.getKey().equals("_id")) {
                // 将 _id 转换为字符串，并重命名为 id
                result.put("id", entry.getValue().toString());
            } else {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    /**
     * 工具方法：将 Document 列表转换为 Map 列表，将 _id 转换为 id 字符串
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
     * 获取用户词书
     */
    public Optional<Map<String, Object>> getUserWordbook(String id) {
        try {
            ObjectId objectId = new ObjectId(id);
            Document document = mongoTemplate.findById(objectId, Document.class, "user_wordbooks");
            return Optional.ofNullable(document)
                    .map(this::convertDocumentToMap);
        } catch (IllegalArgumentException e) {
            log.error("Invalid ObjectId format: {}", id, e);
            return Optional.empty();
        }
    }

    /**
     * 分页获取用户的词书列表
     */
    public Page<Map<String, Object>> getUserWordbooks(String userId, int page, int size, Map<String, String> filters) {
        // 1. 创建干净的过滤条件
        Map<String, String> cleanFilters = filters != null ? new HashMap<>(filters) : new HashMap<>();
        cleanFilters.remove("page");
        cleanFilters.remove("size");

        // 2. 构建基本查询
        Query query = new Query(Criteria.where("createUser").is(userId));

        // 3. 添加其他过滤条件
        cleanFilters.entrySet().stream()
                .filter(entry -> StringUtils.hasText(entry.getValue()))
                .forEach(entry -> query.addCriteria(
                        Criteria.where(entry.getKey()).is(entry.getValue())
                ));

        // 4. 计算总记录数
        long total = mongoTemplate.count(query, "user_wordbooks");

        // 5. 添加明确的排序条件
        query.with(Sort.by(Sort.Direction.DESC, "createTime"));

        // 6. 添加分页
        query.with(PageRequest.of(page, size));

        // 7. 执行查询
        List<Document> documents = mongoTemplate.find(query, Document.class, "user_wordbooks");
        List<Map<String, Object>> wordbooks = convertDocumentsToMaps(documents);

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

        // 如果有id字段，处理为_id (MongoDB格式)
        if (wordbookData.containsKey("id")) {
            Object idValue = wordbookData.remove("id");
            try {
                if (idValue instanceof String) {
                    wordbookData.put("_id", new ObjectId((String) idValue));
                } else {
                    wordbookData.put("_id", idValue);
                }
            } catch (IllegalArgumentException e) {
                log.warn("Invalid ObjectId format for id: {}, will be auto-generated", idValue);
            }
        }

        Document doc = new Document(wordbookData);
        Document savedDocument = mongoTemplate.save(doc, "user_wordbooks");

        if (savedDocument == null) {
            throw new RuntimeException("Failed to create user wordbook");
        }

        return convertDocumentToMap(savedDocument);
    }

    /**
     * 更新用户词书
     */
    public Optional<Map<String, Object>> updateUserWordbook(String id, String userId, Map<String, Object> updateData) {
        try {
            ObjectId objectId = new ObjectId(id);

            // 验证所有权
            Query query = new Query(Criteria.where("_id").is(objectId).and("createUser").is(userId));
            if (!mongoTemplate.exists(query, "user_wordbooks")) {
                return Optional.empty();
            }

            Update update = new Update();

            // 移除保护字段和id字段
            updateData.remove("id");
            updateData.remove("_id");
            updateData.remove("createUser");
            updateData.remove("createTime");
            updateData.remove("status");

            // 应用其他更新
            updateData.forEach(update::set);

            FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);

            Document updatedDocument = mongoTemplate.findAndModify(query, update, options, Document.class, "user_wordbooks");
            return Optional.ofNullable(updatedDocument)
                    .map(this::convertDocumentToMap);
        } catch (IllegalArgumentException e) {
            log.error("Invalid ObjectId format: {}", id, e);
            return Optional.empty();
        }
    }

    /**
     * 删除用户词书
     */
    public boolean deleteUserWordbook(String id, String userId) {
        try {
            ObjectId objectId = new ObjectId(id);
            Query query = new Query(Criteria.where("_id").is(objectId).and("createUser").is(userId));
            return mongoTemplate.remove(query, "user_wordbooks").getDeletedCount() > 0;
        } catch (IllegalArgumentException e) {
            log.error("Invalid ObjectId format: {}", id, e);
            return false;
        }
    }

    /**
     * 更新词书审核状态（管理员操作）
     */
    public Optional<Map<String, Object>> updateWordbookStatus(String id, String status) {
        if (!Arrays.asList("pending", "approved", "rejected").contains(status)) {
            throw new IllegalArgumentException("Invalid status");
        }

        try {
            ObjectId objectId = new ObjectId(id);
            Query query = new Query(Criteria.where("_id").is(objectId));
            Update update = new Update().set("status", status);

            // 创建选项对象，设置返回更新后的文档
            FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);

            Document updatedDocument = mongoTemplate.findAndModify(query, update, options, Document.class, "user_wordbooks");
            return Optional.ofNullable(updatedDocument)
                    .map(this::convertDocumentToMap);
        } catch (IllegalArgumentException e) {
            log.error("Invalid ObjectId format: {}", id, e);
            return Optional.empty();
        }
    }

    /**
     * 获取已审核的公开词书
     */
    public Page<Map<String, Object>> getPublicWordbooks(int page, int size) {
        Query query = new Query(Criteria.where("isPublic").is(true)
                .and("status").is("approved"))
                .with(PageRequest.of(page, size));

        long total = mongoTemplate.count(query, "user_wordbooks");
        List<Document> documents = mongoTemplate.find(query, Document.class, "user_wordbooks");
        List<Map<String, Object>> wordbooks = convertDocumentsToMaps(documents);

        return new PageImpl<>(wordbooks, PageRequest.of(page, size), total);
    }

    /**
     * 添加单词到词书
     */
    public Optional<Map<String, Object>> addWordsToWordbook(String id, String userId, List<String> wordIds) {
        try {
            // 验证所有单词是否存在 - 使用正确的方法
            List<Map<String, Object>> words = wordService.getWordsByIds(wordIds);

            if (words.size() != wordIds.size()) {
                log.error("Some words do not exist. Requested IDs: {}, Found words: {}", wordIds, words.size());
                throw new RuntimeException("Some words do not exist");
            }

            ObjectId objectId = new ObjectId(id);
            Query query = new Query(Criteria.where("_id").is(objectId).and("createUser").is(userId));
            Update update = new Update().addToSet("words").each(wordIds.toArray());

            // 创建选项对象并设置返回更新后的文档
            FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);

            Document updatedDocument = mongoTemplate.findAndModify(query, update, options, Document.class, "user_wordbooks");
            return Optional.ofNullable(updatedDocument)
                    .map(this::convertDocumentToMap);
        } catch (IllegalArgumentException e) {
            log.error("Invalid ObjectId format: {}", id, e);
            return Optional.empty();
        }
    }

    /**
     * 从词书中移除单词
     */
    public Optional<Map<String, Object>> removeWordsFromWordbook(String id, String userId, List<String> wordIds) {
        try {
            ObjectId objectId = new ObjectId(id);
            Query query = new Query(Criteria.where("_id").is(objectId).and("createUser").is(userId));
            Update update = new Update().pullAll("words", wordIds.toArray());

            // 创建选项对象并设置返回更新后的文档
            FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);

            Document updatedDocument = mongoTemplate.findAndModify(query, update, options, Document.class, "user_wordbooks");
            return Optional.ofNullable(updatedDocument)
                    .map(this::convertDocumentToMap);
        } catch (IllegalArgumentException e) {
            log.error("Invalid ObjectId format: {}", id, e);
            return Optional.empty();
        }
    }

    /**
     * 获取词书中的所有单词
     */
    public List<Map<String, Object>> getWordbookWords(String id, String userId) {
        try {
            ObjectId objectId = new ObjectId(id);
            Query query = new Query(Criteria.where("_id").is(objectId));

            if (userId != null) {
                query.addCriteria(Criteria.where("createUser").is(userId));
            }

            Document wordbookDocument = mongoTemplate.findOne(query, Document.class, "user_wordbooks");
            if (wordbookDocument == null) {
                return Collections.emptyList();
            }

            Map<String, Object> wordbook = convertDocumentToMap(wordbookDocument);

            @SuppressWarnings("unchecked")
            List<String> wordIds = (List<String>) wordbook.get("words");
            if (wordIds == null || wordIds.isEmpty()) {
                return Collections.emptyList();
            }

            Map<String, String> queryParams = new HashMap<>();
            queryParams.put("id", String.join(",", wordIds));  // 使用 id 而不是 _id
            return wordService.getWords(queryParams);
        } catch (IllegalArgumentException e) {
            log.error("Invalid ObjectId format: {}", id, e);
            return Collections.emptyList();
        }
    }

    /**
     * 搜索词书
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
        List<Document> documents = mongoTemplate.find(query, Document.class, "user_wordbooks");
        List<Map<String, Object>> wordbooks = convertDocumentsToMaps(documents);

        return new PageImpl<>(wordbooks, pageRequest, total);
    }

    /**
     * 高级搜索词书
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
        List<Document> documents = mongoTemplate.find(query, Document.class, "user_wordbooks");
        List<Map<String, Object>> wordbooks = convertDocumentsToMaps(documents);

        return new PageImpl<>(wordbooks, pageRequest, total);
    }

    /**
     * 根据语言获取已审核的公开词书（不包含单词列表）
     */
    public List<Map<String, Object>> getApprovedPublicWordbooksByLanguage(String language) {
        Query query = new Query();
        query.addCriteria(Criteria.where("language").is(language));
        query.addCriteria(Criteria.where("isPublic").is(true));
        query.addCriteria(Criteria.where("status").is("approved"));

        List<Document> documents = mongoTemplate.find(query, Document.class, "user_wordbooks");
        return convertDocumentsToMaps(documents);
    }
}