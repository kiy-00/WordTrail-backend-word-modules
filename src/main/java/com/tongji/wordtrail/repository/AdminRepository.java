package com.tongji.wordtrail.repository;

import com.tongji.wordtrail.model.Administer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Administer, Integer> {
    Optional<Administer> findByUsername(String username);
    Optional<Administer> findByEmail(String email);
    Optional<Administer> findByAdminKey(String adminKey);
}
