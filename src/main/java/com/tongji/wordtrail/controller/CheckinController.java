package com.tongji.wordtrail.controller;

import com.tongji.wordtrail.service.CheckinService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/checkin/")
public class CheckinController {
    private final CheckinService checkinService;
    public CheckinController(CheckinService checkinService) {
        this.checkinService = checkinService;
    }
    // 1. 获取签到日期（比如返回 [3, 4]）
    @GetMapping("/days")
    public List<Integer> getCheckinDays(@RequestParam String userId) {
        return checkinService.getCheckinDaysInMonth(userId);
    }

    // 2. 获取本月签到天数
    @GetMapping("/count")
    public int getCheckinCount(@RequestParam String userId) {
        return checkinService.getCheckinCountInMonth(userId);
    }

    // 3. 用户签到
    @PostMapping("/userCheckin")
    public String checkIn(@RequestParam String userId) {
        return checkinService.checkIn(userId);
    }
}
