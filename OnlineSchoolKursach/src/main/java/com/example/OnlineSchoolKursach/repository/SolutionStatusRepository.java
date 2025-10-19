package com.example.OnlineSchoolKursach.repository;

import com.example.OnlineSchoolKursach.model.SolutionStatusModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SolutionStatusRepository extends JpaRepository<SolutionStatusModel, Long> {
}

