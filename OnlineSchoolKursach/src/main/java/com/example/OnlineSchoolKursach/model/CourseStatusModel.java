package com.example.OnlineSchoolKursach.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "course_statuses")
public class CourseStatusModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_status_id")
    private Long courseStatusId;

    @NotBlank
    @Size(max = 30)
    @Column(name = "status_name", nullable = false)
    private String statusName;

    public CourseStatusModel() {}

    public CourseStatusModel(String statusName) {
        this.statusName = statusName;
    }

    public Long getCourseStatusId() {
        return courseStatusId;
    }

    public void setCourseStatusId(Long courseStatusId) {
        this.courseStatusId = courseStatusId;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }
}
