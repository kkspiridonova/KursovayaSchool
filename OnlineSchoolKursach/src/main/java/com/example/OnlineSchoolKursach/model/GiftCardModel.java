package com.example.OnlineSchoolKursach.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "gift_cards")
@Schema(description = "Модель подарочной карты")
public class GiftCardModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gift_card_id")
    @Schema(description = "Идентификатор подарочной карты", example = "1")
    private Long giftCardId;

    @NotBlank
    @Size(max = 50)
    @Column(name = "card_number", unique = true, nullable = false)
    @Schema(description = "Номер подарочной карты", example = "GC-1234567890")
    private String cardNumber;

    @DecimalMin(value = "0.0", inclusive = false)
    @Column(name = "balance", precision = 10, scale = 2, nullable = false)
    @Schema(description = "Баланс карты", example = "100.00")
    private BigDecimal balance;

    @NotNull
    @Column(name = "issue_date", nullable = false)
    @Schema(description = "Дата выпуска карты", example = "2023-01-01")
    private LocalDate issueDate;

    @Column(name = "expiry_date")
    @Schema(description = "Дата истечения срока действия", example = "2024-01-01")
    private LocalDate expiryDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    @Schema(description = "Пользователь, которому принадлежит карта")
    private UserModel user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gift_card_status_id", nullable = false)
    @Schema(description = "Статус подарочной карты")
    private GiftCardStatusModel giftCardStatus;

    public GiftCardModel() {}

    public GiftCardModel(String cardNumber, BigDecimal balance, LocalDate issueDate, GiftCardStatusModel giftCardStatus) {
        this.cardNumber = cardNumber;
        this.balance = balance;
        this.issueDate = issueDate;
        this.giftCardStatus = giftCardStatus;
    }

    public Long getGiftCardId() {
        return giftCardId;
    }

    public void setGiftCardId(Long giftCardId) {
        this.giftCardId = giftCardId;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public UserModel getUser() {
        return user;
    }

    public void setUser(UserModel user) {
        this.user = user;
    }

    public GiftCardStatusModel getGiftCardStatus() {
        return giftCardStatus;
    }

    public void setGiftCardStatus(GiftCardStatusModel giftCardStatus) {
        this.giftCardStatus = giftCardStatus;
    }
}