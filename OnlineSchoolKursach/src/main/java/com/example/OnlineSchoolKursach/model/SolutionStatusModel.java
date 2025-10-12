package com.example.OnlineSchoolKursach.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "solution_statuses")
public class SolutionStatusModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "solution_status_id")
    private Long solutionStatusId;

    @NotBlank
    @Size(max = 30)
    @Column(name = "status_name", nullable = false)
    private String statusName;

    public SolutionStatusModel() {}

    public SolutionStatusModel(String statusName) {
        this.statusName = statusName;
    }

    public Long getSolutionStatusId() {
        return solutionStatusId;
    }

    public void setSolutionStatusId(Long solutionStatusId) {
        this.solutionStatusId = solutionStatusId;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }
}
