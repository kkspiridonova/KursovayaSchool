package com.example.OnlineSchoolKursach.repository;

import com.example.OnlineSchoolKursach.model.CourseStatusModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseStatusRepository extends JpaRepository<CourseStatusModel, Long> {
}
