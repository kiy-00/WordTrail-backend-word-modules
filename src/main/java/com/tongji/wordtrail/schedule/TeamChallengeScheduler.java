package com.tongji.wordtrail.schedule;

import com.tongji.wordtrail.model.TeamChallenge;
import com.tongji.wordtrail.model.TeamChallengeDailyResult;
import com.tongji.wordtrail.repository.TeamChallengeDailyResultRepository;
import com.tongji.wordtrail.repository.TeamChallengeRepository;
import com.tongji.wordtrail.repository.TeamChallengeClockInRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Calendar;
import java.util.List;

@Component
public class TeamChallengeScheduler {

    @Autowired
    private TeamChallengeRepository challengeRepository;

    @Autowired
    private TeamChallengeClockInRepository clockInRepository;

    @Autowired
    private TeamChallengeDailyResultRepository dailyResultRepository;

    /**
     * 每天23:59执行，检查当日的挑战打卡状态
     */
    @Scheduled(cron = "0 59 23 * * ?")
    @Transactional
    public void checkDailyClockInStatus() {
        // 获取当前日期
        Date today = new Date();

        // 获取所有活跃状态的挑战
        List<TeamChallenge> activeChallenges = challengeRepository.findByStatus("active");

        for (TeamChallenge challenge : activeChallenges) {
            // 检查挑战是否在有效期内
            if (today.after(challenge.getEndDate())) {
                // 挑战已结束，更新状态
                updateChallengeStatus(challenge);
                continue;
            }

            // 获取当日两位用户的打卡记录
            int completedCount = clockInRepository.countSuccessfulClockInsForDate(challenge.getId(), today);

            // 记录每日打卡结果(可能需要新建表或添加字段)
            recordDailyResult(challenge.getId(), today, completedCount == 2);
        }
    }

    /**
     * 更新挑战状态
     */
    private void updateChallengeStatus(TeamChallenge challenge) {
        // 计算挑战期间成功打卡的天数
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(challenge.getStartDate());
        Date endDate = challenge.getEndDate();

        int totalDays = 0;
        int successDays = 0;

        while (!calendar.getTime().after(endDate)) {
            Date currentDate = calendar.getTime();
            totalDays++;

            int completedCount = clockInRepository.countSuccessfulClockInsForDate(challenge.getId(), currentDate);
            if (completedCount == 2) {
                successDays++;
            }

            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        // 计算成功率
        double successRate = (double) successDays / totalDays;

        // 如果成功率大于阈值（这里假设是80%），标记为完成，否则标记为失败
        if (successRate >= 0.8) {
            challenge.setStatus("completed");
        } else {
            challenge.setStatus("failed");
        }

        challengeRepository.save(challenge);
    }

    /**
     * 记录每日结果
     */
    private void recordDailyResult(Long challengeId, Date date, boolean bothCompleted) {
        // 使用JPA方式实现 - 假设你已经创建了相应的实体类和Repository
        TeamChallengeDailyResult dailyResult = dailyResultRepository
                .findByChallengeIdAndResultDate(challengeId, date)
                .orElse(new TeamChallengeDailyResult());

        dailyResult.setChallengeId(challengeId);
        dailyResult.setResultDate(date);
        dailyResult.setBothCompleted(bothCompleted);

        // 如果是第一次记录，设置创建时间
        if (dailyResult.getId() == null) {
            dailyResult.setCreateTime(new Date());
        }

        dailyResultRepository.save(dailyResult);

        // 如果挑战结束，更新挑战状态
        TeamChallenge challenge = challengeRepository.findById(challengeId).orElse(null);
        if (challenge != null && date.compareTo(challenge.getEndDate()) >= 0) {
            updateChallengeStatus(challenge);
        }
    }
}