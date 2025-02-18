package com.tongji.wordtrail.repository;

import com.tongji.wordtrail.model.UserCheckin;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Date;
import java.util.Optional;

public interface UserCheckinRepository extends JpaRepository<UserCheckin, Long> {
    Optional<UserCheckin> findByUserIdAndCheckinDate(String userId, Date checkinDate);
}
