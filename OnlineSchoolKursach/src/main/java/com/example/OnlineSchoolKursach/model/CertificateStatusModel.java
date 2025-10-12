package com.example.OnlineSchoolKursach.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "certificate_statuses")
public class CertificateStatusModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "certificate_status_id")
    private Long certificateStatusId;

    @NotBlank
    @Size(max = 30)
    @Column(name = "status_name", nullable = false)
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
