package com.example.OnlineSchoolKursach.repository;

import com.example.OnlineSchoolKursach.model.LessonModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<LessonModel, Long> {
    List<LessonModel> findByCourseCourseId(Long courseId);
    List<LessonModel> findByLessonStatusLessonStatusId(Long statusId);
}
