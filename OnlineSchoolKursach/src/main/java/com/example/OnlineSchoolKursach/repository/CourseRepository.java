package com.example.OnlineSchoolKursach.repository;

import com.example.OnlineSchoolKursach.model.CourseModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<CourseModel, Long> {
    List<CourseModel> findByTeacherUserId(Long teacherId);
    List<CourseModel> findByCourseStatusCourseStatusId(Long statusId);
}
