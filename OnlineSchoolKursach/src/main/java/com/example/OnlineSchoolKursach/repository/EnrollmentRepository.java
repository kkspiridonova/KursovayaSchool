package com.example.OnlineSchoolKursach.repository;

import com.example.OnlineSchoolKursach.model.EnrollmentModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnrollmentRepository extends JpaRepository<EnrollmentModel, Long> {
    List<EnrollmentModel> findByUserUserId(Long userId);
    List<EnrollmentModel> findByCourseCourseId(Long courseId);
    List<EnrollmentModel> findByEnrollmentStatusEnrollmentStatusId(Long statusId);
}

