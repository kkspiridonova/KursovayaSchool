package com.example.OnlineSchoolKursach.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "lessons")
@Schema(description = "Модель урока")
public class LessonModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lesson_id")
    @Schema(description = "Идентификатор урока", example = "1")
    private Long lessonId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id", nullable = false)
    @Schema(description = "Курс, к которому относится урок")
    private CourseModel course;

    @NotBlank
    @Size(max = 100)
    @Column(name = "title", nullable = false)
    @Schema(description = "Название урока", example = "Введение в математику")
    private String title;

    @NotBlank
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    @Schema(description = "Содержание урока", example = "Этот урок охватывает основы математики...")
    private String content;

    @Size(max = 255)
    @Column(name = "attached_file")
    @Schema(description = "Прикрепленный файл", example = "/path/to/material.pdf")
    private String attachedFile;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lesson_status_id", nullable = false)
    @Schema(description = "Статус урока")
    private LessonStatusModel lessonStatus;

    public LessonModel() {}

    public LessonModel(CourseModel course, String title, String content, LessonStatusModel lessonStatus) {
        this.course = course;
        this.title = title;
        this.content = content;
        this.lessonStatus = lessonStatus;
    }

    public Long getLessonId() {
        return lessonId;
    }

    public void setLessonId(Long lessonId) {
        this.lessonId = lessonId;
    }

    public CourseModel getCourse() {
        return course;
    }

    public void setCourse(CourseModel course) {
        this.course = course;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAttachedFile() {
        return attachedFile;
    }

    public void setAttachedFile(String attachedFile) {
        this.attachedFile = attachedFile;
    }

    public LessonStatusModel getLessonStatus() {
        return lessonStatus;
    }

    public void setLessonStatus(LessonStatusModel lessonStatus) {
        this.lessonStatus = lessonStatus;
    }
}