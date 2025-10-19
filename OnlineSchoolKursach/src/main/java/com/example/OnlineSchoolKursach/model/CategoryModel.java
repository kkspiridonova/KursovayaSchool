package com.example.OnlineSchoolKursach.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "categories")
@Schema(description = "Модель категории курса")
public class CategoryModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    @Schema(description = "Идентификатор категории", example = "1")
    private Long categoryId;

    @NotBlank
    @Size(max = 50)
    @Column(name = "category_name", nullable = false, unique = true)
    @Schema(description = "Название категории", example = "Математика")
    private String categoryName;

    @Size(max = 200)
    @Column(name = "description", columnDefinition = "TEXT")
    @Schema(description = "Описание категории", example = "Математические дисциплины")
    private String description;

    public CategoryModel() {}

    public CategoryModel(String categoryName, String description) {
        this.categoryName = categoryName;
        this.description = description;
    }
    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}