package com.example.OnlineSchoolKursach.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "lesson_statuses")
public class LessonStatusModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lesson_status_id")
    private Long lessonStatusId;

    @NotBlank
    @Size(max = 30)
    @Column(name = "status_name", nullable = false)
    private String statusName;

    public LessonStatusModel() {}

    public LessonStatusModel(String statusName) {
        this.statusName = statusName;
    }

    public Long getLessonStatusId() {
        return lessonStatusId;
    }

    public void setLessonStatusId(Long lessonStatusId) {
        this.lessonStatusId = lessonStatusId;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }
}

