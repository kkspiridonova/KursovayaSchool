package com.example.OnlineSchoolKursach.repository;

import com.example.OnlineSchoolKursach.model.GiftCardModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GiftCardRepository extends JpaRepository<GiftCardModel, Long> {
    List<GiftCardModel> findByUserUserId(Long userId);
    List<GiftCardModel> findByGiftCardStatusGiftCardStatusId(Long statusId);
    GiftCardModel findByCardNumber(String cardNumber);
    GiftCardModel findByCode(String code);
    
    @Query("SELECT DISTINCT g FROM GiftCardModel g LEFT JOIN FETCH g.giftCardStatus LEFT JOIN FETCH g.course LEFT JOIN FETCH g.user WHERE g.code = :code")
    GiftCardModel findByCodeWithDetails(@Param("code") String code);
}