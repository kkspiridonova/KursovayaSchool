package com.example.OnlineSchoolKursach.repository;

import com.example.OnlineSchoolKursach.model.CertificateStatusModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CertificateStatusRepository extends JpaRepository<CertificateStatusModel, Long> {
}
