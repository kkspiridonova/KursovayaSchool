package com.example.OnlineSchoolKursach.repository;

import com.example.OnlineSchoolKursach.model.UserSettingsModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserSettingsRepository extends JpaRepository<UserSettingsModel, Long> {
    Optional<UserSettingsModel> findByUserUserId(Long userId);
    void deleteByUserUserId(Long userId);
}

