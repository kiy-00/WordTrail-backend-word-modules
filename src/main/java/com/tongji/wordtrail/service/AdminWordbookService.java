package com.tongji.wordtrail.service;

import com.tongji.wordtrail.dto.AdminWordResponse;
import com.tongji.wordtrail.dto.AdminWordbooksResponse;
import com.tongji.wordtrail.model.SystemWordbook;
import com.tongji.wordtrail.model.Words;
import com.tongji.wordtrail.repository.AdminWordbookRepository;
import com.tongji.wordtrail.repository.SystemWordbookRepository;
import com.tongji.wordtrail.repository.WordRepository;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
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



    public List<AdminWordbooksResponse> findWordBooks() {
        logger.debug("Finding wordbooks...");
        // 获取所有词书
        List<SystemWordbook> wordbooks = adminWordbookRepository.findAll();
        if (wordbooks.isEmpty()) {
            logger.error("No wordbooks found");
            throw new RuntimeException("No wordbooks found");
        }

        // 转换为 AdminWordbooksResponse
        List<AdminWordbooksResponse> responseList = wordbooks.stream()
                .map(wordbook -> new AdminWordbooksResponse(
                        wordbook.getBookName(),
                        wordbook.getLanguage(),
                        wordbook.getDescription(),
                        wordbook.getWordCount()
                ))
                .collect(Collectors.toList());

        logger.info("Found {} wordbooks", responseList.size());
        return responseList;
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
}
