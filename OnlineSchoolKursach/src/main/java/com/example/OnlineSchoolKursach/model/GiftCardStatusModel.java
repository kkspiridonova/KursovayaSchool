package com.example.OnlineSchoolKursach.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "gift_card_statuses")
public class GiftCardStatusModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gift_card_status_id")
    private Long giftCardStatusId;

    @NotBlank
    @Size(max = 30)
    @Column(name = "status_name", nullable = false)
    private String statusName;

    public GiftCardStatusModel() {}

    public GiftCardStatusModel(String statusName) {
        this.statusName = statusName;
    }

    public Long getGiftCardStatusId() {
        return giftCardStatusId;
    }

    public void setGiftCardStatusId(Long giftCardStatusId) {
        this.giftCardStatusId = giftCardStatusId;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }
}
