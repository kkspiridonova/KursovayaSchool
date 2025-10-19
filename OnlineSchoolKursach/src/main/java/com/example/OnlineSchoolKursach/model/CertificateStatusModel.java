package com.example.OnlineSchoolKursach.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "certificate_statuses")
@Schema(description = "Модель статуса сертификата")
public class CertificateStatusModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "certificate_status_id")
    @Schema(description = "Идентификатор статуса сертификата", example = "1")
    private Long certificateStatusId;

    @NotBlank
    @Size(max = 30)
    @Column(name = "status_name", nullable = false)
    @Schema(description = "Название статуса сертификата", example = "Выдан")
    private String statusName;

    public CertificateStatusModel() {}

    public CertificateStatusModel(String statusName) {
        this.statusName = statusName;
    }

    public Long getCertificateStatusId() {
        return certificateStatusId;
    }

    public void setCertificateStatusId(Long certificateStatusId) {
        this.certificateStatusId = certificateStatusId;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }
}