package com.example.OnlineSchoolKursach.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_settings")
@Schema(description = "Модель настроек пользователя")
public class UserSettingsModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_settings_id")
    @Schema(description = "Идентификатор настроек", example = "1")
    private Long userSettingsId;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @NotNull
    @Schema(description = "Пользователь")
    private UserModel user;

    @Column(name = "theme", length = 20)
    @Schema(description = "Тема интерфейса", example = "dark", allowableValues = {"light", "dark"})
    private String theme = "light";

    @Column(name = "items_per_page")
    @Min(1)
    @Max(100)
    @Schema(description = "Количество элементов на странице", example = "10")
    private Integer itemsPerPage = 10;

    @Column(name = "date_format", length = 20)
    @Schema(description = "Формат даты", example = "dd.MM.yyyy")
    private String dateFormat = "dd.MM.yyyy";

    @Column(name = "saved_filters", columnDefinition = "TEXT")
    @Schema(description = "Сохраненные фильтры в формате JSON")
    private String savedFilters;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Schema(description = "Дата создания")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @Schema(description = "Дата обновления")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public UserSettingsModel() {
    }

    public UserSettingsModel(UserModel user) {
        this.user = user;
        this.theme = "light";
        this.itemsPerPage = 10;
        this.dateFormat = "dd.MM.yyyy";
    }

    public Long getUserSettingsId() {
        return userSettingsId;
    }

    public void setUserSettingsId(Long userSettingsId) {
        this.userSettingsId = userSettingsId;
    }

    public UserModel getUser() {
        return user;
    }

    public void setUser(UserModel user) {
        this.user = user;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public Integer getItemsPerPage() {
        return itemsPerPage;
    }

    public void setItemsPerPage(Integer itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getSavedFilters() {
        return savedFilters;
    }

    public void setSavedFilters(String savedFilters) {
        this.savedFilters = savedFilters;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

