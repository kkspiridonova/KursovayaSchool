package com.example.OnlineSchoolKursach.repository;

import com.example.OnlineSchoolKursach.model.LessonStatusModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonStatusRepository extends JpaRepository<LessonStatusModel, Long> {
}
