package com.tongji.wordtrail.service;

import com.tongji.wordtrail.model.TeamChallenge;
import com.tongji.wordtrail.model.TeamChallengeClockIn;
import com.tongji.wordtrail.model.LearningRecord;
import com.tongji.wordtrail.model.UserFriend;
import com.tongji.wordtrail.repository.TeamChallengeClockInRepository;
import com.tongji.wordtrail.repository.TeamChallengeRepository;
import com.tongji.wordtrail.repository.UserFriendRepository;
import com.tongji.wordtrail.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.text.SimpleDateFormat;
import java.util.Calendar;

@Service
public class TeamChallengeService {

    private final TeamChallengeRepository challengeRepository;
    private final TeamChallengeClockInRepository clockInRepository;
    private final UserFriendRepository friendRepository;
    private final UserRepository userRepository;

    @Autowired
    private LearningRecordService learningRecordService;

    @Autowired
    public TeamChallengeService(
            TeamChallengeRepository challengeRepository,
            TeamChallengeClockInRepository clockInRepository,
            UserFriendRepository friendRepository,
            UserRepository userRepository) {
        this.challengeRepository = challengeRepository;
        this.clockInRepository = clockInRepository;
        this.friendRepository = friendRepository;
        this.userRepository = userRepository;
    }

    /**
     * 创建组队挑战
     */
    public TeamChallenge createChallenge(String creatorId, String partnerId, String name,
                                         String description, int dailyWordsTarget, int durationDays) {
        // 检查是否为好友关系
        UserFriend friendship = friendRepository.findByUserIdAndFriendId(creatorId, partnerId)
                .orElseThrow(() -> new IllegalArgumentException("只能与好友创建组队挑战"));

        // 验证用户存在
        userRepository.findById(creatorId)
                .orElseThrow(() -> new IllegalArgumentException("创建者用户不存在"));
        userRepository.findById(partnerId)
                .orElseThrow(() -> new IllegalArgumentException("伙伴用户不存在"));

        // 创建挑战
        TeamChallenge challenge = new TeamChallenge();
        challenge.setCreatorId(creatorId);
        challenge.setPartnerId(partnerId);
        challenge.setName(name);
        challenge.setDescription(description);
        challenge.setDailyWordsTarget(dailyWordsTarget);
        challenge.setStatus("pending"); // 等待伙伴接受

        // 设置开始日期为今天
        Calendar calendar = Calendar.getInstance();
        Date startDate = calendar.getTime();
        challenge.setStartDate(startDate);

        // 设置结束日期
        calendar.add(Calendar.DAY_OF_MONTH, durationDays);
        Date endDate = calendar.getTime();
        challenge.setEndDate(endDate);

        return challengeRepository.save(challenge);
    }

    /**
     * 接受组队挑战
     */
    @Transactional
    public TeamChallenge acceptChallenge(Long challengeId, String userId) {
        TeamChallenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("挑战不存在"));

        // 验证是否为被邀请的伙伴
        if (!challenge.getPartnerId().equals(userId)) {
            throw new IllegalArgumentException("无权接受此挑战");
        }

        // 验证挑战状态
        if (!"pending".equals(challenge.getStatus())) {
            throw new IllegalArgumentException("此挑战状态非待接受状态");
        }

        // 更新挑战状态为活跃
        challenge.setStatus("active");
        challenge.setUpdateTime(new Date());
        return challengeRepository.save(challenge);
    }

    /**
     * 拒绝组队挑战
     */
    public TeamChallenge rejectChallenge(Long challengeId, String userId) {
        TeamChallenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("挑战不存在"));

        // 验证是否为被邀请的伙伴
        if (!challenge.getPartnerId().equals(userId)) {
            throw new IllegalArgumentException("无权拒绝此挑战");
        }

        // 验证挑战状态
        if (!"pending".equals(challenge.getStatus())) {
            throw new IllegalArgumentException("此挑战状态非待接受状态");
        }

        // 更新挑战状态为拒绝
        challenge.setStatus("rejected");
        challenge.setUpdateTime(new Date());
        return challengeRepository.save(challenge);
    }

    /**
     * 获取用户参与的所有挑战
     */
    public List<Map<String, Object>> getUserChallenges(String userId) {
        List<TeamChallenge> challenges = challengeRepository.findByUserIdInvolved(userId);

        return challenges.stream().map(challenge -> {
            Map<String, Object> challengeInfo = new HashMap<>();
            challengeInfo.put("challengeId", challenge.getId());
            challengeInfo.put("name", challenge.getName());
            challengeInfo.put("description", challenge.getDescription());
            challengeInfo.put("status", challenge.getStatus());
            challengeInfo.put("dailyWordsTarget", challenge.getDailyWordsTarget());
            challengeInfo.put("streakDays", challenge.getStreakDays());
            challengeInfo.put("startDate", challenge.getStartDate());
            challengeInfo.put("endDate", challenge.getEndDate());

            // 确定用户角色
            boolean isCreator = challenge.getCreatorId().equals(userId);
            challengeInfo.put("isCreator", isCreator);

            // 添加伙伴信息
            String partnerId = isCreator ? challenge.getPartnerId() : challenge.getCreatorId();
            challengeInfo.put("partnerId", partnerId);

            userRepository.findById(partnerId).ifPresent(partner -> {
                challengeInfo.put("partnerUsername", partner.getUsername());
                challengeInfo.put("partnerAvatar", partner.getAvatarUrl());
            });

            // 添加今日打卡状态
            Date today = new Date();
            Optional<TeamChallengeClockIn> todayClockIn = clockInRepository
                    .findByChallengeIdAndUserIdAndClockInDate(challenge.getId(), userId, today);

            challengeInfo.put("todayClockInStatus", todayClockIn.isPresent() && todayClockIn.get().isStatus());
            challengeInfo.put("wordsCompleted", todayClockIn.map(TeamChallengeClockIn::getWordsCompleted).orElse(0));

            // 添加伙伴今日打卡状态
            Optional<TeamChallengeClockIn> partnerTodayClockIn = clockInRepository
                    .findByChallengeIdAndUserIdAndClockInDate(challenge.getId(), partnerId, today);

            challengeInfo.put("partnerTodayClockInStatus", partnerTodayClockIn.isPresent() && partnerTodayClockIn.get().isStatus());

            return challengeInfo;
        }).collect(Collectors.toList());
    }

    /**
     * 获取单个挑战详情
     */
    public Map<String, Object> getChallengeDetail(Long challengeId, String userId) {
        TeamChallenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("挑战不存在"));

        // 验证用户参与此挑战
        if (!challenge.getCreatorId().equals(userId) && !challenge.getPartnerId().equals(userId)) {
            throw new IllegalArgumentException("无权查看此挑战");
        }

        Map<String, Object> challengeInfo = new HashMap<>();
        challengeInfo.put("challengeId", challenge.getId());
        challengeInfo.put("name", challenge.getName());
        challengeInfo.put("description", challenge.getDescription());
        challengeInfo.put("status", challenge.getStatus());
        challengeInfo.put("dailyWordsTarget", challenge.getDailyWordsTarget());
        challengeInfo.put("streakDays", challenge.getStreakDays());
        challengeInfo.put("startDate", challenge.getStartDate());
        challengeInfo.put("endDate", challenge.getEndDate());

        // 确定用户角色
        boolean isCreator = challenge.getCreatorId().equals(userId);
        challengeInfo.put("isCreator", isCreator);

        // 添加伙伴信息
        String partnerId = isCreator ? challenge.getPartnerId() : challenge.getCreatorId();
        challengeInfo.put("partnerId", partnerId);

        userRepository.findById(partnerId).ifPresent(partner -> {
            challengeInfo.put("partnerUsername", partner.getUsername());
            challengeInfo.put("partnerAvatar", partner.getAvatarUrl());
        });

        // 添加今日打卡状态
        Date today = new Date();
        Optional<TeamChallengeClockIn> todayClockIn = clockInRepository
                .findByChallengeIdAndUserIdAndClockInDate(challenge.getId(), userId, today);

        challengeInfo.put("todayClockInStatus", todayClockIn.isPresent() && todayClockIn.get().isStatus());
        challengeInfo.put("wordsCompleted", todayClockIn.map(TeamChallengeClockIn::getWordsCompleted).orElse(0));

        // 添加伙伴今日打卡状态
        Optional<TeamChallengeClockIn> partnerTodayClockIn = clockInRepository
                .findByChallengeIdAndUserIdAndClockInDate(challenge.getId(), partnerId, today);

        challengeInfo.put("partnerTodayClockInStatus", partnerTodayClockIn.isPresent() && partnerTodayClockIn.get().isStatus());

        // 获取过去一周的打卡记录
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -6); // 过去7天(包括今天)
        Date weekAgo = calendar.getTime();

        List<Map<String, Object>> weeklyRecords = new ArrayList<>();
        calendar.setTime(weekAgo);

        for (int i = 0; i < 7; i++) {
            Date currentDate = calendar.getTime();

            Map<String, Object> dailyRecord = new HashMap<>();
            dailyRecord.put("date", new SimpleDateFormat("yyyy-MM-dd").format(currentDate));

            // 用户打卡记录
            Optional<TeamChallengeClockIn> userClockIn = clockInRepository
                    .findByChallengeIdAndUserIdAndClockInDate(challenge.getId(), userId, currentDate);
            dailyRecord.put("userClockInStatus", userClockIn.isPresent() && userClockIn.get().isStatus());
            dailyRecord.put("userWordsCompleted", userClockIn.map(TeamChallengeClockIn::getWordsCompleted).orElse(0));

            // 伙伴打卡记录
            Optional<TeamChallengeClockIn> partnerClockIn = clockInRepository
                    .findByChallengeIdAndUserIdAndClockInDate(challenge.getId(), partnerId, currentDate);
            dailyRecord.put("partnerClockInStatus", partnerClockIn.isPresent() && partnerClockIn.get().isStatus());
            dailyRecord.put("partnerWordsCompleted", partnerClockIn.map(TeamChallengeClockIn::getWordsCompleted).orElse(0));

            // 两人是否都完成
            dailyRecord.put("bothCompleted",
                    (userClockIn.isPresent() && userClockIn.get().isStatus()) &&
                            (partnerClockIn.isPresent() && partnerClockIn.get().isStatus()));

            weeklyRecords.add(dailyRecord);

            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        challengeInfo.put("weeklyRecords", weeklyRecords);

        return challengeInfo;
    }

    /**
     * 组队打卡 - 使用系统记录的实际学习数据，只计算"learn"类型的记录
     */
    @Transactional
    public Map<String, Object> clockIn(Long challengeId, String userId) {
        TeamChallenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("挑战不存在"));

        // 验证用户参与此挑战
        if (!challenge.getCreatorId().equals(userId) && !challenge.getPartnerId().equals(userId)) {
            throw new IllegalArgumentException("无权参与此挑战打卡");
        }

        // 验证挑战状态
        if (!"active".equals(challenge.getStatus())) {
            throw new IllegalArgumentException("只能对活跃状态的挑战进行打卡");
        }

        // 验证挑战日期
        Date today = new Date();
        if (today.after(challenge.getEndDate())) {
            throw new IllegalArgumentException("挑战已结束，无法打卡");
        }

        // 获取用户今日学习记录并计算学习单词数量
        List<LearningRecord> todayRecords = learningRecordService.getTodayLearningRecords(userId);

        // 只计算"learn"类型的记录
        int wordsCompleted = todayRecords.stream()
                .filter(record -> "learn".equals(record.getType()))
                .mapToInt(LearningRecord::getCount)
                .sum();

        // 获取今日的打卡记录，若不存在则创建
        TeamChallengeClockIn clockIn = clockInRepository
                .findByChallengeIdAndUserIdAndClockInDate(challengeId, userId, today)
                .orElse(new TeamChallengeClockIn(challengeId, userId, today));

        // 更新打卡信息
        clockIn.setWordsCompleted(wordsCompleted);

        // 判断是否达成目标
        boolean achieved = wordsCompleted >= challenge.getDailyWordsTarget();
        clockIn.setStatus(achieved);
        clockIn.setUpdateTime(new Date());
        clockInRepository.save(clockIn);

        // 检查伙伴是否也完成了今天的挑战
        String partnerId = challenge.getCreatorId().equals(userId) ? challenge.getPartnerId() : challenge.getCreatorId();
        Optional<TeamChallengeClockIn> partnerClockIn = clockInRepository
                .findByChallengeIdAndUserIdAndClockInDate(challengeId, partnerId, today);

        boolean partnerAchieved = partnerClockIn.isPresent() && partnerClockIn.get().isStatus();

        // 如果两人都完成，更新连续打卡天数
        if (achieved && partnerAchieved) {
            // 检查昨天是否也完成了打卡
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            Date yesterday = calendar.getTime();

            int yesterdayCompletedCount = clockInRepository
                    .countSuccessfulClockInsForDate(challengeId, yesterday);

            // 如果昨天两人都完成了或者是第一天打卡，增加连续天数
            if (yesterdayCompletedCount == 2 || challenge.getStreakDays() == 0) {
                challenge.setStreakDays(challenge.getStreakDays() + 1);
                challengeRepository.save(challenge);
            }
        }

        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("achieved", achieved);
        result.put("partnerAchieved", partnerAchieved);
        result.put("bothAchieved", achieved && partnerAchieved);
        result.put("streakDays", challenge.getStreakDays());
        result.put("wordsCompleted", wordsCompleted);
        result.put("dailyWordsTarget", challenge.getDailyWordsTarget());

        return result;
    }

    /**
     * 获取用户的活跃挑战
     */
    public List<Map<String, Object>> getActiveUserChallenges(String userId) {
        List<TeamChallenge> challenges = challengeRepository.findActiveByUserIdInvolved(userId);

        return challenges.stream().map(challenge -> {
            Map<String, Object> challengeInfo = new HashMap<>();
            challengeInfo.put("challengeId", challenge.getId());
            challengeInfo.put("name", challenge.getName());
            challengeInfo.put("dailyWordsTarget", challenge.getDailyWordsTarget());
            challengeInfo.put("streakDays", challenge.getStreakDays());

            // 确定用户角色
            boolean isCreator = challenge.getCreatorId().equals(userId);

            // 添加伙伴信息
            String partnerId = isCreator ? challenge.getPartnerId() : challenge.getCreatorId();

            userRepository.findById(partnerId).ifPresent(partner -> {
                challengeInfo.put("partnerUsername", partner.getUsername());
                challengeInfo.put("partnerAvatar", partner.getAvatarUrl());
            });

            // 添加今日打卡状态
            Date today = new Date();
            Optional<TeamChallengeClockIn> todayClockIn = clockInRepository
                    .findByChallengeIdAndUserIdAndClockInDate(challenge.getId(), userId, today);

            challengeInfo.put("todayClockInStatus", todayClockIn.isPresent() && todayClockIn.get().isStatus());

            return challengeInfo;
        }).collect(Collectors.toList());
    }

    /**
     * 获取挑战统计信息
     */
    public Map<String, Object> getChallengeStats(Long challengeId, String userId) {
        TeamChallenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("挑战不存在"));

        // 验证用户参与此挑战
        if (!challenge.getCreatorId().equals(userId) && !challenge.getPartnerId().equals(userId)) {
            throw new IllegalArgumentException("无权查看此挑战统计");
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("challengeId", challenge.getId());
        stats.put("name", challenge.getName());
        stats.put("status", challenge.getStatus());
        stats.put("streakDays", challenge.getStreakDays());

        // 计算总打卡天数和总单词量
        List<TeamChallengeClockIn> userClockIns = clockInRepository
                .findByChallengeIdAndUserId(challengeId, userId);

        int totalClockInDays = (int) userClockIns.stream()
                .filter(TeamChallengeClockIn::isStatus)
                .count();

        int totalWords = userClockIns.stream()
                .mapToInt(TeamChallengeClockIn::getWordsCompleted)
                .sum();

        stats.put("totalClockInDays", totalClockInDays);
        stats.put("totalWords", totalWords);

        // 计算平均每天学习单词数
        double averageWordsPerDay = totalClockInDays > 0 ?
                (double) totalWords / totalClockInDays : 0;
        stats.put("averageWordsPerDay", Math.round(averageWordsPerDay * 100) / 100.0);

        // 伙伴信息
        String partnerId = challenge.getCreatorId().equals(userId) ?
                challenge.getPartnerId() : challenge.getCreatorId();

        userRepository.findById(partnerId).ifPresent(partner -> {
            stats.put("partnerUsername", partner.getUsername());
        });

        // 计算伙伴的总打卡天数和总单词量
        List<TeamChallengeClockIn> partnerClockIns = clockInRepository
                .findByChallengeIdAndUserId(challengeId, partnerId);

        int partnerTotalClockInDays = (int) partnerClockIns.stream()
                .filter(TeamChallengeClockIn::isStatus)
                .count();

        int partnerTotalWords = partnerClockIns.stream()
                .mapToInt(TeamChallengeClockIn::getWordsCompleted)
                .sum();

        stats.put("partnerTotalClockInDays", partnerTotalClockInDays);
        stats.put("partnerTotalWords", partnerTotalWords);

        // 计算双方共同完成的天数
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(challenge.getStartDate());
        Date endDate = new Date();
        if (endDate.after(challenge.getEndDate())) {
            endDate = challenge.getEndDate();
        }

        int bothCompletedDays = 0;
        while (!calendar.getTime().after(endDate)) {
            Date currentDate = calendar.getTime();
            int completedCount = clockInRepository
                    .countSuccessfulClockInsForDate(challengeId, currentDate);

            if (completedCount == 2) {
                bothCompletedDays++;
            }

            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        stats.put("bothCompletedDays", bothCompletedDays);

        return stats;
    }

    /**
     * 获取当日打卡状态
     */
    public Map<String, Object> getTodayClockInStatus(Long challengeId, String userId) {
        TeamChallenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("挑战不存在"));

        // 验证用户参与此挑战
        if (!challenge.getCreatorId().equals(userId) && !challenge.getPartnerId().equals(userId)) {
            throw new IllegalArgumentException("无权查看此挑战");
        }

        Date today = new Date();
        Map<String, Object> result = new HashMap<>();

        // 确定伙伴ID
        String partnerId = challenge.getCreatorId().equals(userId) ?
                challenge.getPartnerId() : challenge.getCreatorId();

        // 获取用户打卡状态
        Optional<TeamChallengeClockIn> userClockIn = clockInRepository
                .findByChallengeIdAndUserIdAndClockInDate(challengeId, userId, today);

        boolean userCompleted = userClockIn.isPresent() && userClockIn.get().isStatus();
        int userWordsCompleted = userClockIn.map(TeamChallengeClockIn::getWordsCompleted).orElse(0);

        // 获取伙伴打卡状态
        Optional<TeamChallengeClockIn> partnerClockIn = clockInRepository
                .findByChallengeIdAndUserIdAndClockInDate(challengeId, partnerId, today);

        boolean partnerCompleted = partnerClockIn.isPresent() && partnerClockIn.get().isStatus();
        int partnerWordsCompleted = partnerClockIn.map(TeamChallengeClockIn::getWordsCompleted).orElse(0);

        // 填充结果
        result.put("challengeId", challengeId);
        result.put("date", new SimpleDateFormat("yyyy-MM-dd").format(today));
        result.put("userCompleted", userCompleted);
        result.put("userWordsCompleted", userWordsCompleted);
        result.put("partnerCompleted", partnerCompleted);
        result.put("partnerWordsCompleted", partnerWordsCompleted);
        result.put("dailyTarget", challenge.getDailyWordsTarget());
        result.put("bothCompleted", userCompleted && partnerCompleted);

        return result;
    }

    /**
     * 获取挑战最终结果
     */
    public Map<String, Object> getChallengeResult(Long challengeId, String userId) {
        TeamChallenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("挑战不存在"));

        // 验证用户参与此挑战
        if (!challenge.getCreatorId().equals(userId) && !challenge.getPartnerId().equals(userId)) {
            throw new IllegalArgumentException("无权查看此挑战");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("challengeId", challengeId);
        result.put("name", challenge.getName());
        result.put("status", challenge.getStatus());
        result.put("dailyWordsTarget", challenge.getDailyWordsTarget());
        result.put("streakDays", challenge.getStreakDays());
        result.put("startDate", challenge.getStartDate());
        result.put("endDate", challenge.getEndDate());

        // 计算挑战期间的总天数
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(challenge.getStartDate());
        Date endDate = challenge.getEndDate();

        int totalDays = 0;
        int successDays = 0;

        List<Map<String, Object>> dailyRecords = new ArrayList<>();

        while (!calendar.getTime().after(endDate)) {
            Date currentDate = calendar.getTime();
            totalDays++;

            Map<String, Object> dailyRecord = new HashMap<>();
            dailyRecord.put("date", new SimpleDateFormat("yyyy-MM-dd").format(currentDate));

            int completedCount = clockInRepository
                    .countSuccessfulClockInsForDate(challenge.getId(), currentDate);

            boolean daySuccessful = completedCount == 2;
            if (daySuccessful) {
                successDays++;
            }

            dailyRecord.put("bothCompleted", daySuccessful);
            dailyRecords.add(dailyRecord);

            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        result.put("totalDays", totalDays);
        result.put("successDays", successDays);
        result.put("successRate", Math.round((double) successDays / totalDays * 100) / 100.0);
        result.put("dailyRecords", dailyRecords);

        // 添加用户和伙伴信息
        String partnerId = challenge.getCreatorId().equals(userId) ?
                challenge.getPartnerId() : challenge.getCreatorId();

        // 计算用户统计
        int userTotalWords = clockInRepository
                .findByChallengeIdAndUserId(challengeId, userId)
                .stream()
                .mapToInt(TeamChallengeClockIn::getWordsCompleted)
                .sum();

        int partnerTotalWords = clockInRepository
                .findByChallengeIdAndUserId(challengeId, partnerId)
                .stream()
                .mapToInt(TeamChallengeClockIn::getWordsCompleted)
                .sum();

        result.put("userTotalWords", userTotalWords);
        result.put("partnerTotalWords", partnerTotalWords);

        return result;
    }
}