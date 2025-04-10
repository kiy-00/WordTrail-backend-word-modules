package com.tongji.wordtrail.repository;

import com.tongji.wordtrail.model.TeamChallengeDailyResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;
import java.util.List;

@Repository
public interface TeamChallengeDailyResultRepository extends JpaRepository<TeamChallengeDailyResult, Long> {
    Optional<TeamChallengeDailyResult> findByChallengeIdAndResultDate(Long challengeId, Date resultDate);

    List<TeamChallengeDailyResult> findByChallengeIdOrderByResultDateAsc(Long challengeId);

    long countByChallengeIdAndBothCompletedTrue(Long challengeId);
}