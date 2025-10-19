package com.example.OnlineSchoolKursach.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Entity
@Table(name = "courses")
@Schema(description = "Модель курса")
public class CourseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    @Schema(description = "Идентификатор курса", example = "1")
    private Long courseId;

    @NotBlank
    @Size(max = 100)
    @Column(name = "title", nullable = false)
    @Schema(description = "Название курса", example = "Математика для начинающих")
    private String title;

    @NotBlank
    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    @Schema(description = "Описание курса", example = "Базовый курс математики для студентов")
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "teacher_id", nullable = false)
    @Schema(description = "Преподаватель курса")
    private UserModel teacher;

    @DecimalMin(value = "0.0", inclusive = false)
    @Column(name = "price", precision = 10, scale = 2, nullable = false)
    @Schema(description = "Цена курса", example = "99.99")
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_status_id", nullable = false)
    @Schema(description = "Статус курса")
    private CourseStatusModel courseStatus;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    @Schema(description = "Категория курса")
    private CategoryModel category;

    @Column(name = "image_url", length = 1000)
    @Schema(description = "URL изображения курса")
    private String imageUrl;

    public CourseModel() {}

    public CourseModel(String title, String description, UserModel teacher, BigDecimal price, CourseStatusModel courseStatus, CategoryModel category) {
        this.title = title;
        this.description = description;
        this.teacher = teacher;
        this.price = price;
        this.courseStatus = courseStatus;
        this.category = category;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UserModel getTeacher() {
        return teacher;
    }

    public void setTeacher(UserModel teacher) {
        this.teacher = teacher;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public CourseStatusModel getCourseStatus() {
        return courseStatus;
    }

    public void setCourseStatus(CourseStatusModel courseStatus) {
        this.courseStatus = courseStatus;
    }

    public CategoryModel getCategory() {
        return category;
    }

    public void setCategory(CategoryModel category) {
        this.category = category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
