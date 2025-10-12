package com.example.OnlineSchoolKursach.repository;

import com.example.OnlineSchoolKursach.model.CheckModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CheckRepository extends JpaRepository<CheckModel, Long> {
    List<CheckModel> findByUserUserId(Long userId);
    List<CheckModel> findByCourseCourseId(Long courseId);
    List<CheckModel> findByPaymentStatusPaymentStatusId(Long statusId);
}
