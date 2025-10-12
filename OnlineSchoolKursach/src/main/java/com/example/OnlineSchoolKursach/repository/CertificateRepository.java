package com.example.OnlineSchoolKursach.repository;

import com.example.OnlineSchoolKursach.model.CertificateModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CertificateRepository extends JpaRepository<CertificateModel, Long> {
    List<CertificateModel> findByUserUserId(Long userId);
    List<CertificateModel> findByCourseCourseId(Long courseId);
    List<CertificateModel> findByCertificateStatusCertificateStatusId(Long statusId);
}
