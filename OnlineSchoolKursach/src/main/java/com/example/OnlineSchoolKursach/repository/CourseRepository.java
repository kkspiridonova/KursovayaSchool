package com.example.OnlineSchoolKursach.repository;

import com.example.OnlineSchoolKursach.model.CourseModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<CourseModel, Long> {
    List<CourseModel> findByTeacherUserId(Long userId);
    List<CourseModel> findByCategoryCategoryId(Long categoryId);
    
    @Query(value = "SELECT calculate_course_revenue(:courseId)", nativeQuery = true)
    BigDecimal calculateCourseRevenue(@Param("courseId") Long courseId);
}
