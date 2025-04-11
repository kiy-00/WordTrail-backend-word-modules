package com.tongji.wordtrail.service;

import com.tongji.wordtrail.model.WordLearningProgress;
import com.tongji.wordtrail.model.SystemWordbook;
import com.tongji.wordtrail.model.UserWordbook;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WordLearningProgressService {

    private final MongoTemplate mongoTemplate;
    private static final int[] REVIEW_INTERVALS = {1, 2, 4, 7, 15, 30};

    @Autowired
    public WordLearningProgressService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public WordLearningProgress startLearningWord(String userId, ObjectId wordId) {
        Query query = new Query(Criteria.where("userId").is(userId)
                .and("wordId").is(wordId));

        WordLearningProgress progress = mongoTemplate.findOne(query, WordLearningProgress.class);
        if (progress == null) {
            progress = new WordLearningProgress(userId, wordId);
            progress = mongoTemplate.save(progress);
        }
        return progress;
    }

    public WordLearningProgress recordReviewResult(String userId, ObjectId wordId, boolean remembered) {
        Query query = new Query(Criteria.where("userId").is(userId)
                .and("wordId").is(wordId));

        WordLearningProgress progress = mongoTemplate.findOne(query, WordLearningProgress.class);
        if (progress == null) {
            return null;
        }

        progress.addReviewHistory(remembered);

        if (remembered) {
            if (progress.getReviewStage() < REVIEW_INTERVALS.length - 1) {
                progress.setReviewStage(progress.getReviewStage() + 1);
            }
            progress.setProficiency(calculateNewProficiency(progress.getProficiency(), true));
        } else {
            if (progress.getReviewStage() > 0) {
                progress.setReviewStage(progress.getReviewStage() - 1);
            }
            progress.setProficiency(calculateNewProficiency(progress.getProficiency(), false));
        }

        Date nextReview = calculateNextReviewTime(progress.getLastReviewTime(),
                REVIEW_INTERVALS[progress.getReviewStage()]);
        progress.setNextReviewTime(nextReview);

        return mongoTemplate.save(progress);
    }

    public List<WordLearningProgress> getTodayReviewWords(String userId) {
        Date today = new Date();
        Date startOfDay = new Date(today.getYear(), today.getMonth(), today.getDate());
        Date endOfDay = new Date(startOfDay.getTime() + 24 * 60 * 60 * 1000);

        // 修改查询条件，包括当天和逾期的单词
        Query query = new Query(Criteria.where("userId").is(userId)
                .and("nextReviewTime").lt(endOfDay));  // 去掉 gte(startOfDay) 条件

        return mongoTemplate.find(query, WordLearningProgress.class);
    }

    public List<WordLearningProgress> getTodayReviewWordsForBook(String userId, ObjectId bookId) {
        List<ObjectId> bookWordIds = getBookWordIds(bookId);

        Date today = new Date();
        Date startOfDay = new Date(today.getYear(), today.getMonth(), today.getDate());
        Date endOfDay = new Date(startOfDay.getTime() + 24 * 60 * 60 * 1000);

        // 修改查询条件，包括当天和逾期的单词
        Query query = new Query(Criteria.where("userId").is(userId)
                .and("wordId").in(bookWordIds)
                .and("nextReviewTime").lt(endOfDay));  // 去掉 gte(startOfDay) 条件

        return mongoTemplate.find(query, WordLearningProgress.class);
    }

    public long getOverdueReviewCount(String userId) {
        Query query = new Query(Criteria.where("userId").is(userId)
                .and("nextReviewTime").lte(new Date()));

        return mongoTemplate.count(query, WordLearningProgress.class);
    }

    public long getOverdueReviewCountForBook(String userId, ObjectId bookId) {
        List<ObjectId> bookWordIds = getBookWordIds(bookId);

        Query query = new Query(Criteria.where("userId").is(userId)
                .and("wordId").in(bookWordIds)
                .and("nextReviewTime").lte(new Date()));

        return mongoTemplate.count(query, WordLearningProgress.class);
    }

    public UserLearningStats getUserStats(String userId) {
        Query query = new Query(Criteria.where("userId").is(userId));
        List<WordLearningProgress> allProgress = mongoTemplate.find(query, WordLearningProgress.class);

        UserLearningStats stats = new UserLearningStats();
        stats.setTotalWords(allProgress.size());
        stats.setMasteredWords((int) allProgress.stream()
                .filter(p -> p.getProficiency() >= 0.9).count());
        stats.setLearningWords((int) allProgress.stream()
                .filter(p -> p.getProficiency() > 0 && p.getProficiency() < 0.9).count());
        stats.setAverageProficiency(allProgress.stream()
                .mapToDouble(WordLearningProgress::getProficiency)
                .average().orElse(0.0));

        return stats;
    }

    public BookLearningStats getBookStats(String userId, ObjectId bookId) {
        List<ObjectId> bookWordIds = getBookWordIds(bookId);

        Query query = new Query(Criteria.where("userId").is(userId)
                .and("wordId").in(bookWordIds));
        List<WordLearningProgress> bookProgress = mongoTemplate.find(query, WordLearningProgress.class);

        BookLearningStats stats = new BookLearningStats();
        stats.setTotalWords(bookWordIds.size());
        stats.setLearnedWords(bookProgress.size());
        stats.setMasteredWords((int) bookProgress.stream()
                .filter(p -> p.getProficiency() >= 0.9).count());
        stats.setLearningWords((int) bookProgress.stream()
                .filter(p -> p.getProficiency() > 0 && p.getProficiency() < 0.9).count());
        stats.setAverageProficiency(bookProgress.stream()
                .mapToDouble(WordLearningProgress::getProficiency)
                .average().orElse(0.0));

        return stats;
    }

    public Optional<WordLearningProgress> getWordProgress(String userId, ObjectId wordId) {
        Query query = new Query(Criteria.where("userId").is(userId)
                .and("wordId").is(wordId));
        return Optional.ofNullable(mongoTemplate.findOne(query, WordLearningProgress.class));
    }

    public List<WordLearningProgress> getProgressForWords(String userId, List<ObjectId> wordIds) {
        Query query = new Query(Criteria.where("userId").is(userId)
                .and("wordId").in(wordIds));
        return mongoTemplate.find(query, WordLearningProgress.class);
    }

    public List<WordLearningProgress> getProgressForBook(String userId, ObjectId bookId) {
        List<ObjectId> bookWordIds = getBookWordIds(bookId);
        return getProgressForWords(userId, bookWordIds);
    }

    public List<WordLearningProgress> getBookReviewWords(String userId, ObjectId bookId) {
        List<ObjectId> bookWordIds = getBookWordIds(bookId);

        Query query = new Query(Criteria.where("userId").is(userId)
                .and("wordId").in(bookWordIds)
                .and("nextReviewTime").lte(new Date()));

        return mongoTemplate.find(query, WordLearningProgress.class);
    }

    private double calculateNewProficiency(double currentProficiency, boolean remembered) {
        if (remembered) {
            return Math.min(1.0, currentProficiency + 0.1);
        } else {
            return Math.max(0.0, currentProficiency - 0.1);
        }
    }

    private Date calculateNextReviewTime(Date lastReviewTime, int intervalDays) {
        return new Date(lastReviewTime.getTime() + intervalDays * 24 * 60 * 60 * 1000);
    }

    private List<ObjectId> getBookWordIds(ObjectId bookId) {
        Query systemQuery = new Query(Criteria.where("_id").is(bookId));
        SystemWordbook systemBook = mongoTemplate.findOne(systemQuery, SystemWordbook.class);
        if (systemBook != null) {
            return systemBook.getWords();
        }

        UserWordbook userBook = mongoTemplate.findOne(systemQuery, UserWordbook.class);
        if (userBook != null) {
            return userBook.getWords();
        }

        throw new IllegalArgumentException("Wordbook not found: " + bookId);
    }

    /**
     * 获取指定词书中模糊的单词（熟练度 >= 0.5 且 < 0.8）
     * @param userId 用户ID
     * @param bookId 词书ID
     * @return 模糊单词的学习进度列表
     */
    public List<WordLearningProgress> getFuzzyWordsFromBook(String userId, ObjectId bookId) {
        List<ObjectId> bookWordIds = getBookWordIds(bookId);

        Query query = new Query(Criteria.where("userId").is(userId)
                .and("wordId").in(bookWordIds)
                .and("proficiency").gte(0.5).lt(0.8));

        return mongoTemplate.find(query, WordLearningProgress.class);
    }

    /**
     * 获取指定词书中熟悉的单词（熟练度 >= 0.8 且 <= 1）
     * @param userId 用户ID
     * @param bookId 词书ID
     * @return 熟悉单词的学习进度列表
     */
    public List<WordLearningProgress> getFamiliarWordsFromBook(String userId, ObjectId bookId) {
        List<ObjectId> bookWordIds = getBookWordIds(bookId);

        Query query = new Query(Criteria.where("userId").is(userId)
                .and("wordId").in(bookWordIds)
                .and("proficiency").gte(0.8).lte(1.0));

        return mongoTemplate.find(query, WordLearningProgress.class);
    }

    /**
     * 获取指定词书中未学习的单词（熟练度 = 0）
     * @param userId 用户ID
     * @param bookId 词书ID
     * @return 未学习单词的学习进度列表
     */
    public List<WordLearningProgress> getUnlearnedWordsFromBook(String userId, ObjectId bookId) {
        List<ObjectId> bookWordIds = getBookWordIds(bookId);

        Query query = new Query(Criteria.where("userId").is(userId)
                .and("wordId").in(bookWordIds)
                .and("proficiency").is(0.0));

        return mongoTemplate.find(query, WordLearningProgress.class);
    }

    // 内部类保持不变，因为它们不涉及 userId
    public static class UserLearningStats {
        private int totalWords;
        private int masteredWords;
        private int learningWords;
        private double averageProficiency;

        public int getTotalWords() { return totalWords; }
        public void setTotalWords(int totalWords) { this.totalWords = totalWords; }
        public int getMasteredWords() { return masteredWords; }
        public void setMasteredWords(int masteredWords) { this.masteredWords = masteredWords; }
        public int getLearningWords() { return learningWords; }
        public void setLearningWords(int learningWords) { this.learningWords = learningWords; }
        public double getAverageProficiency() { return averageProficiency; }
        public void setAverageProficiency(double averageProficiency) { this.averageProficiency = averageProficiency; }
    }

    public static class BookLearningStats {
        private int totalWords;
        private int learnedWords;
        private int masteredWords;
        private int learningWords;
        private double averageProficiency;

        public int getTotalWords() { return totalWords; }
        public void setTotalWords(int totalWords) { this.totalWords = totalWords; }
        public int getLearnedWords() { return learnedWords; }
        public void setLearnedWords(int learnedWords) { this.learnedWords = learnedWords; }
        public int getMasteredWords() { return masteredWords; }
        public void setMasteredWords(int masteredWords) { this.masteredWords = masteredWords; }
        public int getLearningWords() { return learningWords; }
        public void setLearningWords(int learningWords) { this.learningWords = learningWords; }
        public double getAverageProficiency() { return averageProficiency; }
        public void setAverageProficiency(double averageProficiency) { this.averageProficiency = averageProficiency; }
    }

    public List<String> getNewWordsFromBook(String userId, ObjectId bookId, int batchSize) {
        // 获取词书中所有的单词ID
        List<ObjectId> allBookWordIds = getBookWordIds(bookId);

        // 查询用户已经学习过的单词
        Query query = new Query(Criteria.where("userId").is(userId)
                .and("wordId").in(allBookWordIds));
        List<WordLearningProgress> learnedWords = mongoTemplate.find(query, WordLearningProgress.class);

        // 提取已学习单词的ID
        List<ObjectId> learnedWordIds = learnedWords.stream()
                .map(WordLearningProgress::getWordId)
                .collect(Collectors.toList());

        // 过滤出未学习的单词，并转换为字符串ID
        List<String> newWordIds = allBookWordIds.stream()
                .filter(wordId -> !learnedWordIds.contains(wordId))
                .limit(batchSize)
                .map(ObjectId::toString)
                .collect(Collectors.toList());

        return newWordIds;
    }

    /**
     * 获取词书中未学习单词的总数
     * @param userId 用户ID
     * @param bookId 词书ID
     * @return 未学习单词数量
     */
    public int getNewWordsCountFromBook(String userId, ObjectId bookId) {
        // 获取词书中所有的单词ID
        List<ObjectId> allBookWordIds = getBookWordIds(bookId);

        // 查询用户已经学习过的单词
        Query query = new Query(Criteria.where("userId").is(userId)
                .and("wordId").in(allBookWordIds));
        List<WordLearningProgress> learnedWords = mongoTemplate.find(query, WordLearningProgress.class);

        // 提取已学习单词的ID
        List<ObjectId> learnedWordIds = learnedWords.stream()
                .map(WordLearningProgress::getWordId)
                .collect(Collectors.toList());

        // 计算未学习的单词数量（Java 8写法）
        long count = allBookWordIds.stream()
                .filter(wordId -> !learnedWordIds.contains(wordId))
                .count();

        return (int) count;
    }

    /**
     * 获取指定词书中今天需要复习的单词数量（包括逾期未复习的单词）
     * @param userId 用户ID
     * @param bookId 词书ID
     * @return 今天需要复习的单词数量（包括逾期）
     */
    public int getTodayReviewWordsCountForBook(String userId, ObjectId bookId) {
        List<ObjectId> bookWordIds = getBookWordIds(bookId);

        Date today = new Date();
        Date endOfDay = new Date(today.getYear(), today.getMonth(), today.getDate() + 1);

        // 修改查询条件，包括当天和逾期的单词
        Query query = new Query(Criteria.where("userId").is(userId)
                .and("wordId").in(bookWordIds)
                .and("nextReviewTime").lt(endOfDay));  // 去掉 gte(startOfDay) 条件

        return (int) mongoTemplate.count(query, WordLearningProgress.class);
    }
}