package com.tongji.wordtrail.service;

import com.tongji.wordtrail.entity.LearningClockIn;
import com.tongji.wordtrail.model.LearningGoal;
import com.tongji.wordtrail.model.WordLearningProgress;
import com.tongji.wordtrail.repository.LearningClockInRepository;
import com.tongji.wordtrail.repository.LearningGoalRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.Calendar;
import java.text.SimpleDateFormat;

@Service
public class LearningClockInService {

    private final LearningClockInRepository clockInRepository;
    private final LearningGoalRepository goalRepository;
    private final MongoTemplate mongoTemplate;
    private final WordLearningProgressService wordLearningProgressService;

    @Autowired
    public LearningClockInService(
            LearningClockInRepository clockInRepository,
            LearningGoalRepository goalRepository,
            MongoTemplate mongoTemplate,
            WordLearningProgressService wordLearningProgressService) {
        this.clockInRepository = clockInRepository;
        this.goalRepository = goalRepository;
        this.mongoTemplate = mongoTemplate;
        this.wordLearningProgressService = wordLearningProgressService;
    }

    /**
     * 设置或更新用户的学习目标
     */
    public LearningGoal setLearningGoal(String userId, int dailyNewWordsGoal, int dailyReviewWordsGoal) {
        Optional<LearningGoal> existingGoal = goalRepository.findByUserId(userId);

        LearningGoal goal;
        if (existingGoal.isPresent()) {
            goal = existingGoal.get();
            goal.setDailyNewWordsGoal(dailyNewWordsGoal);
            goal.setDailyReviewWordsGoal(dailyReviewWordsGoal);
            goal.setUpdatedAt(new Date());
        } else {
            goal = new LearningGoal(userId, dailyNewWordsGoal, dailyReviewWordsGoal);
        }

        return goalRepository.save(goal);
    }

    /**
     * 获取用户的学习目标
     */
    public LearningGoal getLearningGoal(String userId) {
        return goalRepository.findByUserId(userId)
                .orElse(new LearningGoal(userId, 10, 30)); // 默认每天10个新单词，30个复习单词
    }

    /**
     * 获取或创建今日打卡记录
     */
    public LearningClockIn getOrCreateTodayClockIn(String userId) {
        Date today = getTodayDate();
        Optional<LearningClockIn> existingClockIn = clockInRepository.findByUserIdAndClockInDate(userId, today);

        if (existingClockIn.isPresent()) {
            return existingClockIn.get();
        }

        // 创建新的打卡记录
        LearningClockIn clockIn = new LearningClockIn(userId, today);

        // 获取用户的学习目标
        LearningGoal goal = getLearningGoal(userId);
        clockIn.setNewWordsTarget(goal.getDailyNewWordsGoal());
        clockIn.setReviewWordsTarget(goal.getDailyReviewWordsGoal());

        // 获取最近的打卡记录，以计算连续打卡天数
        Optional<LearningClockIn> lastClockIn = clockInRepository.findTopByUserIdOrderByClockInDateDesc(userId);

        if (lastClockIn.isPresent()) {
            // 检查是否是连续打卡
            Date lastDate = lastClockIn.get().getClockInDate();
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            cal1.setTime(lastDate);
            cal2.setTime(today);

            // 判断是否是昨天
            cal1.add(Calendar.DAY_OF_YEAR, 1);
            boolean isConsecutive = isSameDay(cal1, cal2);

            if (isConsecutive && lastClockIn.get().getStatus()) {
                // 如果昨天打卡成功且是连续的，则连续天数加一
                clockIn.setStreakDays(lastClockIn.get().getStreakDays() + 1);
            } else {
                // 否则重置连续天数
                clockIn.setStreakDays(0);
            }
        }

        return clockInRepository.save(clockIn);
    }

    /**
     * 尝试打卡
     */
    @Transactional
    public LearningClockIn tryClockIn(String userId) {
        // 获取或创建今日打卡记录
        LearningClockIn clockIn = getOrCreateTodayClockIn(userId);

        // 如果已经打卡成功，直接返回
        if (clockIn.getStatus()) {
            return clockIn;
        }

        // 获取用户的学习目标
        LearningGoal goal = getLearningGoal(userId);

        // 计算今天学习的新单词数量
        int newWordsLearned = countTodayNewWordsLearned(userId);
        clockIn.setNewWordsCompleted(newWordsLearned);

        // 计算今天复习的单词数量
        int wordsReviewed = countTodayWordsReviewed(userId);
        clockIn.setReviewWordsCompleted(wordsReviewed);

        // 判断是否达标
        boolean newWordsAchieved = newWordsLearned >= goal.getDailyNewWordsGoal();
        boolean reviewWordsAchieved = wordsReviewed >= goal.getDailyReviewWordsGoal();

        // 两个目标都达到才算打卡成功
        clockIn.setStatus(newWordsAchieved && reviewWordsAchieved);

        // 更新打卡信息
        clockIn.setUpdateTime(new Date());

        return clockInRepository.save(clockIn);
    }

    /**
     * 获取用户打卡统计信息
     */
    public Map<String, Object> getUserClockInStats(String userId) {
        Map<String, Object> stats = new HashMap<>();

        // 获取今日打卡状态
        LearningClockIn todayClockIn = getOrCreateTodayClockIn(userId);
        stats.put("todayStatus", todayClockIn.getStatus());
        stats.put("streakDays", todayClockIn.getStreakDays());
        stats.put("newWordsTarget", todayClockIn.getNewWordsTarget());
        stats.put("newWordsCompleted", todayClockIn.getNewWordsCompleted());
        stats.put("reviewWordsTarget", todayClockIn.getReviewWordsTarget());
        stats.put("reviewWordsCompleted", todayClockIn.getReviewWordsCompleted());

        // 可以添加更多统计信息...

        return stats;
    }

    /**
     * 计算今天学习的新单词数量
     */
    private int countTodayNewWordsLearned(String userId) {
        Date today = getTodayDate();
        Date tomorrow = getNextDay(today);

        Query query = new Query(Criteria.where("userId").is(userId)
                .and("firstLearnTime").gte(today).lt(tomorrow));

        return (int) mongoTemplate.count(query, WordLearningProgress.class);
    }

    /**
     * 计算今天复习的单词数量
     */
    private int countTodayWordsReviewed(String userId) {
        Date today = getTodayDate();
        Date tomorrow = getNextDay(today);

        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        query.addCriteria(Criteria.where("reviewHistory").elemMatch(
                Criteria.where("time").gte(today).lt(tomorrow)
        ));

        return (int) mongoTemplate.count(query, WordLearningProgress.class);
    }

    /**
     * 获取当天0点的日期对象
     */
    private Date getTodayDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * 获取下一天的日期对象
     */
    private Date getNextDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_YEAR, 1);
        return cal.getTime();
    }

    /**
     * 判断两个Calendar对象是否表示同一天
     */
    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * 获取用户过去一周的打卡记录
     */
    public List<Map<String, Object>> getWeeklyClockInHistory(String userId) {
        // 计算一周前的日期
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -6); // 包括今天，共7天
        Date weekStart = cal.getTime();

        // 使用原生SQL查询可能更高效，这里简化处理
        // 查询一周内的所有打卡记录
        List<LearningClockIn> weekRecords = clockInRepository.findAll().stream()
                .filter(record -> record.getUserId().equals(userId))
                .filter(record -> record.getClockInDate().after(weekStart) || isSameDay(record.getClockInDate(), weekStart))
                .collect(Collectors.toList());

        // 构建一周每天的打卡记录
        List<Map<String, Object>> result = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        // 处理过去7天的记录
        for (int i = 0; i < 7; i++) {
            Calendar dateCal = Calendar.getInstance();
            dateCal.add(Calendar.DAY_OF_YEAR, -i);
            Date currentDate = dateCal.getTime();
            String dateString = dateFormat.format(currentDate);

            Map<String, Object> dayRecord = new HashMap<>();
            dayRecord.put("date", dateString);

            // 查找这一天的打卡记录
            Optional<LearningClockIn> record = weekRecords.stream()
                    .filter(r -> isSameDay(r.getClockInDate(), currentDate))
                    .findFirst();

            if (record.isPresent()) {
                dayRecord.put("status", record.get().getStatus());
                dayRecord.put("newWordsCompleted", record.get().getNewWordsCompleted());
                dayRecord.put("reviewWordsCompleted", record.get().getReviewWordsCompleted());
            } else {
                dayRecord.put("status", false);
                dayRecord.put("newWordsCompleted", 0);
                dayRecord.put("reviewWordsCompleted", 0);
            }

            result.add(dayRecord);
        }

        return result;
    }

    /**
     * 判断两个日期是否是同一天
     */
    private boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return isSameDay(cal1, cal2);
    }
}