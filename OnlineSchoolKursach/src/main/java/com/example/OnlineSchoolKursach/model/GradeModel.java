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
    @Max(value = 5, message = "Оценка не может быть больше 5")
    @Column(name = "grade_value", nullable = false)
    @Schema(description = "Значение оценки (пятибалльная система)", example = "5")
    private Integer gradeValue;

    @OneToMany(mappedBy = "grade", fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"grade", "task", "user", "solutionStatus"})
    @Schema(description = "Решения, которые получили эту оценку")
    private java.util.List<SolutionModel> solutions;

    @Transient
    @JsonIgnoreProperties({"grade", "task", "user", "solutionStatus"})
    @Schema(description = "Решение из запроса (для десериализации JSON)")
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

    public java.util.List<SolutionModel> getSolutions() {
        return solutions;
    }

    public void setSolutions(java.util.List<SolutionModel> solutions) {
        this.solutions = solutions;
    }

    public SolutionModel getSolution() {
        return solution;
    }

    public void setSolution(SolutionModel solution) {
        this.solution = solution;
    }
}