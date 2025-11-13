package com.example.OnlineSchoolKursach.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Entity
@Table(name = "certificates")
@Schema(description = "Модель сертификата о прохождении курса")
public class CertificateModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "certificate_id")
    @Schema(description = "Идентификатор сертификата", example = "1")
    private Long certificateId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @Schema(description = "Студент, получивший сертификат")
    private UserModel user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id", nullable = false)
    @Schema(description = "Курс, за который выдан сертификат")
    private CourseModel course;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "certificate_status_id", nullable = false)
    @Schema(description = "Статус сертификата")
    private CertificateStatusModel certificateStatus;

    @NotNull
    @Column(name = "certificate_number", unique = true, nullable = false, length = 100)
    @Schema(description = "Уникальный номер сертификата", example = "CERT-1-5-20251111")
    private String certificateNumber;

    @NotNull
    @Column(name = "issue_date", nullable = false)
    @Schema(description = "Дата выдачи сертификата", example = "2025-11-11")
    private LocalDate issueDate;

    @Column(name = "file_path", length = 1000)
    @Schema(description = "Путь к файлу сертификата в MinIO")
    private String filePath;

    @Column(name = "document_file", length = 1000, nullable = false)
    @Schema(description = "Путь к документу сертификата (дублирует file_path для совместимости с БД)")
    private String documentFile;

    @Column(name = "email_sent", nullable = false)
    @Schema(description = "Отправлен ли сертификат на почту")
    private Boolean emailSent = false;

    @Column(name = "email_sent_date")
    @Schema(description = "Дата отправки сертификата на почту")
    private LocalDate emailSentDate;

    public CertificateModel() {
        this.issueDate = LocalDate.now();
        this.emailSent = false;
    }

    public CertificateModel(UserModel user, CourseModel course, String certificateNumber) {
        this();
        this.user = user;
        this.course = course;
        this.certificateNumber = certificateNumber;
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

    public CertificateStatusModel getCertificateStatus() {
        return certificateStatus;
    }

    public void setCertificateStatus(CertificateStatusModel certificateStatus) {
        this.certificateStatus = certificateStatus;
    }

    public String getCertificateNumber() {
        return certificateNumber;
    }

    public void setCertificateNumber(String certificateNumber) {
        this.certificateNumber = certificateNumber;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
        // Синхронизируем documentFile с filePath
        this.documentFile = filePath;
    }

    public String getDocumentFile() {
        return documentFile;
    }

    public void setDocumentFile(String documentFile) {
        this.documentFile = documentFile;
    }

    public Boolean getEmailSent() {
        return emailSent;
    }

    public void setEmailSent(Boolean emailSent) {
        this.emailSent = emailSent;
    }

    public LocalDate getEmailSentDate() {
        return emailSentDate;
    }

    public void setEmailSentDate(LocalDate emailSentDate) {
        this.emailSentDate = emailSentDate;
    }
}
