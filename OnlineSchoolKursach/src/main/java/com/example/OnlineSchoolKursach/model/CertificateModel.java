package com.example.OnlineSchoolKursach.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Entity
@Table(name = "certificates")
public class CertificateModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "certificate_id")
    private Long certificateId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private UserModel user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id", nullable = false)
    private CourseModel course;

    @NotNull
    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @NotBlank
    @Size(max = 255)
    @Column(name = "document_file", nullable = false)
    private String documentFile;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "certificate_status_id", nullable = false)
    private CertificateStatusModel certificateStatus;

    public CertificateModel() {}

    public CertificateModel(UserModel user, CourseModel course, LocalDate issueDate, String documentFile, CertificateStatusModel certificateStatus) {
        this.user = user;
        this.course = course;
        this.issueDate = issueDate;
        this.documentFile = documentFile;
        this.certificateStatus = certificateStatus;
    }

    public Long getCertificateId() {
        return certificateId;
    }

    public void setCertificateId(Long certificateId) {
        this.certificateId = certificateId;
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

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public String getDocumentFile() {
        return documentFile;
    }

    public void setDocumentFile(String documentFile) {
        this.documentFile = documentFile;
    }

    public CertificateStatusModel getCertificateStatus() {
        return certificateStatus;
    }

    public void setCertificateStatus(CertificateStatusModel certificateStatus) {
        this.certificateStatus = certificateStatus;
    }
}
