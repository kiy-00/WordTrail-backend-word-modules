package com.tongji.wordtrail.service;

import com.tongji.wordtrail.model.UserCheckin;
import com.tongji.wordtrail.repository.CheckinRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CheckinService {
    @Autowired
    private CheckinRepository checkinRepository;

    public List<Integer> getCheckinDaysInMonth(String userId) {
        LocalDate now = LocalDate.now();
        List<UserCheckin> checkins = checkinRepository.findByUserIdAndMonth(userId, now.getMonthValue(), now.getYear());

        return checkins.stream()
                .map(c -> {
                    Date date = c.getCheckinDate();
                    if (date instanceof java.sql.Date) {
                        return ((java.sql.Date) date).toLocalDate().getDayOfMonth();
                    } else {
                        // fallback: 适用于 java.util.Date
                        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getDayOfMonth();
                    }
                })
                .collect(Collectors.toList());

    }

    public int getCheckinCountInMonth(String userId) {
        return getCheckinDaysInMonth(userId).size();
    }

    public String checkIn(String userId) {
        Date today = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());

        if (checkinRepository.hasCheckedInToday(userId, today)) {
            return "今日已签到";
        }

        UserCheckin checkin = new UserCheckin();
        checkin.setUserId(userId);
        checkin.setCheckinDate(today);
        checkin.setCreateTime(new Date());
        checkin.setCheckinDays(1); // 可后续计算连续签到

        checkinRepository.save(checkin);
        return "签到成功";
    }
}
