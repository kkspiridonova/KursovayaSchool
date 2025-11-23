package com.example.OnlineSchoolKursach.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Entity
@Table(name = "enrollments")
@Schema(description = "Модель записи на курс")
public class EnrollmentModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enrollment_id")
    @Schema(description = "Идентификатор записи", example = "1")
    private Long enrollmentId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @Schema(description = "Пользователь, записавшийся на курс")
    private UserModel user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id", nullable = false)
    @Schema(description = "Курс, на который записались")
    private CourseModel course;

    @NotNull
    @Column(name = "enrollment_date", nullable = false)
    @Schema(description = "Дата записи на курс", example = "2023-01-01")
    private LocalDate enrollmentDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "enrollment_status_id", nullable = false)
    @Schema(description = "Статус записи")
    private EnrollmentStatusModel enrollmentStatus;

    public EnrollmentModel() {}

    public EnrollmentModel(UserModel user, CourseModel course, LocalDate enrollmentDate, EnrollmentStatusModel enrollmentStatus) {
        this.user = user;
        this.course = course;
        this.enrollmentDate = enrollmentDate;
        this.enrollmentStatus = enrollmentStatus;
    }

    public Long getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(Long enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public UserModel getUser() {
        return user;
    }

    public void setUser(UserModel user) {
        this.user = user;
    }

    public CourseModel getCourse() {
        return course;
    }

    public void setCourse(CourseModel course) {
        this.course = course;
    }

    public LocalDate getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(LocalDate enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public EnrollmentStatusModel getEnrollmentStatus() {
        return enrollmentStatus;
    }

    public void setEnrollmentStatus(EnrollmentStatusModel enrollmentStatus) {
        this.enrollmentStatus = enrollmentStatus;
    }
}
