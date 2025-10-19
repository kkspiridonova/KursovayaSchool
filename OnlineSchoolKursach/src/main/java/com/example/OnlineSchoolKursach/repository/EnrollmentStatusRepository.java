package com.example.OnlineSchoolKursach.repository;

import com.example.OnlineSchoolKursach.model.EnrollmentStatusModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnrollmentStatusRepository extends JpaRepository<EnrollmentStatusModel, Long> {
}

