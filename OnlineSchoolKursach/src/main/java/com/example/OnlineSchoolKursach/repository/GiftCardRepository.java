package com.example.OnlineSchoolKursach.repository;

import com.example.OnlineSchoolKursach.model.GiftCardModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GiftCardRepository extends JpaRepository<GiftCardModel, Long> {
    List<GiftCardModel> findByUserUserId(Long userId);
    List<GiftCardModel> findByCourseCourseId(Long courseId);
    List<GiftCardModel> findByGiftCardStatusGiftCardStatusId(Long statusId);
    GiftCardModel findByCode(String code);
}
