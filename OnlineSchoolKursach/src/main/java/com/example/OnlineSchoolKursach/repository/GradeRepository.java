package com.example.OnlineSchoolKursach.repository;

import com.example.OnlineSchoolKursach.model.GradeModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GradeRepository extends JpaRepository<GradeModel, Long> {
}

