package com.example.OnlineSchoolKursach.repository;

import com.example.OnlineSchoolKursach.model.GiftCardStatusModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GiftCardStatusRepository extends JpaRepository<GiftCardStatusModel, Long> {
}
