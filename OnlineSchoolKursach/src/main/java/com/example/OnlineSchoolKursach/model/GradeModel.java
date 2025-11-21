package com.example.OnlineSchoolKursach.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "grades")
@Schema(description = "Модель оценки")
public class GradeModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "grade_id")
    @Schema(description = "Идентификатор оценки", example = "1")
    private Long gradeId;

    @NotNull
    @Min(value = 0, message = "Оценка не может быть меньше 0")
    @Max(value = 100, message = "Оценка не может быть больше 100")
    @Column(name = "grade_value", nullable = false)
    @Schema(description = "Значение оценки", example = "85")
    private Integer gradeValue;

    @OneToOne(mappedBy = "grade", fetch = FetchType.EAGER)
    @JsonIgnoreProperties({"grade", "task", "user", "solutionStatus"})
    @Schema(description = "Решение, которое получило эту оценку")
    private SolutionModel solution;

    public GradeModel() {}

    public GradeModel(Integer gradeValue) {
        this.gradeValue = gradeValue;
    }

    public Long getGradeId() {
        return gradeId;
    }

    public void setGradeId(Long gradeId) {
        this.gradeId = gradeId;
    }

    public Integer getGradeValue() {
        return gradeValue;
    }

    public void setGradeValue(Integer gradeValue) {
        this.gradeValue = gradeValue;
    }

    public SolutionModel getSolution() {
        return solution;
    }

    public void setSolution(SolutionModel solution) {
        this.solution = solution;
    }
}