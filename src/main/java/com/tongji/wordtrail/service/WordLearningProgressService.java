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

        Query query = new Query(Criteria.where("userId").is(userId)
                .and("nextReviewTime").gte(startOfDay).lt(endOfDay));

        return mongoTemplate.find(query, WordLearningProgress.class);
    }

    public List<WordLearningProgress> getTodayReviewWordsForBook(String userId, ObjectId bookId) {
        List<ObjectId> bookWordIds = getBookWordIds(bookId);

        Date today = new Date();
        Date startOfDay = new Date(today.getYear(), today.getMonth(), today.getDate());
        Date endOfDay = new Date(startOfDay.getTime() + 24 * 60 * 60 * 1000);

        Query query = new Query(Criteria.where("userId").is(userId)
                .and("wordId").in(bookWordIds)
                .and("nextReviewTime").gte(startOfDay).lt(endOfDay));

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
}