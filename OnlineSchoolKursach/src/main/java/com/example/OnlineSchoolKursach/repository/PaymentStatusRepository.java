package com.example.OnlineSchoolKursach.repository;

import com.example.OnlineSchoolKursach.model.PaymentStatusModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentStatusRepository extends JpaRepository<PaymentStatusModel, Long> {
}

