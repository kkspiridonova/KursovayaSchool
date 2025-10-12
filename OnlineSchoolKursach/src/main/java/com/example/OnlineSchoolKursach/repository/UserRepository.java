package com.example.OnlineSchoolKursach.repository;

import com.example.OnlineSchoolKursach.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserModel, Long> {
    Optional<UserModel> findByEmail(String email);
    Boolean existsByEmail(String email);
}

