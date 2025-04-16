package com.tongji.wordtrail.service;

import com.tongji.wordtrail.dto.AdminWordRequest;
import com.tongji.wordtrail.dto.AdminWordResponse;
import com.tongji.wordtrail.dto.AdminWordbookRequest;
import com.tongji.wordtrail.dto.AdminWordbooksResponse;
import com.tongji.wordtrail.model.SystemWordbook;
import com.tongji.wordtrail.model.Words;
import com.tongji.wordtrail.repository.AdminWordbookRepository;
import com.tongji.wordtrail.repository.SystemWordbookRepository;
import com.tongji.wordtrail.repository.WordRepository;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminWordbookService {
    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    @Autowired
    private AdminWordbookRepository adminWordbookRepository;
    @Autowired
    private WordRepository wordRepository;
    @Autowired
    private SystemWordbookRepository systemWordbookRepository;
    @Autowired
    private SystemWordbookService systemWordbookService;;
    @Autowired
    private MongoTemplate mongoTemplate;


    public List<Map<String, Object>> findWordbooks() {
        try {
            // 查询所有的词书
            List<Document> documents = mongoTemplate.findAll(Document.class, "system_wordbooks");

            // 转换成符合需求的结果列表
            return documents.stream()
                    .map(document -> {
                        Map<String, Object> result = new HashMap<>();

                        // 处理_id字段，转换为字符串
                        result.put("id", document.getObjectId("_id").toString());

                        // 复制其他普通字段
                        result.put("bookName", document.getString("bookName"));
                        result.put("language", document.getString("language"));
                        result.put("description", document.getString("description"));

                        // 计算词书的词数
                        List<ObjectId> words = (List<ObjectId>) document.get("words");
                        int wordCount = (words != null) ? words.size() : 0;
                        result.put("wordCount", wordCount);

                        return result;
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("Error retrieving wordbooks", e);
            return Collections.emptyList();
        }
    }

    public List<Words> findWords(ObjectId wordbookId) {
        SystemWordbook wordbook = systemWordbookRepository.findById(wordbookId).orElse(null);
        if (wordbook == null || wordbook.getWordCount() == 0) {
            logger.error("No wordbook found with id: {}", wordbookId);
            throw new RuntimeException("No wordbook found with id: " + wordbookId);
        }
        List<Words> words= wordRepository.findByIdIn(wordbook.getWords());
        if (words.isEmpty()) {
            logger.error("No words found with id: {}", wordbookId);
            throw new RuntimeException("No words found with id: " + wordbookId);
        }
        return words;

    }
    /**
     * 添加单词
     */
    public Words createWord(Words word) {
        return mongoTemplate.save(word, "words");
    }
    // 创建词书
    public List<ObjectId> createWord(Map<String, Object> wordbookData) {
        List<Map<String, Object>> wordList = (List<Map<String, Object>>) wordbookData.get("words");
        List<ObjectId> insertedIds = new ArrayList<>();

        for (Map<String, Object> wordMap : wordList) {
            Words word = new Words();
            word.setWord((String) wordMap.get("word"));
            word.setLanguage((String) wordMap.get("language"));
            word.setPhonetics((List<Words.Phonetic>) wordMap.get("phonetics"));
            word.setSynonyms((List<String>) wordMap.get("synonyms"));
            word.setAntonyms((List<String>) wordMap.get("antonyms"));
            word.setPartOfSpeechList((List<Words.PartOfSpeech>) wordMap.get("partOfSpeechList"));
            word.setTags((List<String>) wordMap.get("tags"));
            word.setDifficulty((Integer) wordMap.get("difficulty"));

            Words saved = mongoTemplate.save(word, "words");
            insertedIds.add(saved.getId());  // 拿到 MongoDB 自动生成的 id
        }

        return insertedIds;
    }
    public Map<String, Object> createSystemWordbook(Map<String, Object> wordbookData) {
        List<ObjectId> words = createWord(wordbookData);
        wordbookData.put("createUser", "system");
        wordbookData.remove("words");
        wordbookData.put("words", words);
        return systemWordbookService.createSystemWordbook(wordbookData);
    }


}
