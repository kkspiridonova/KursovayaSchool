package com.example.OnlineSchoolKursach.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Entity
@Table(name = "solutions")
@Schema(description = "Модель решения задания")
public class SolutionModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "solution_id")
    @Schema(description = "Идентификатор решения", example = "1")
    private Long solutionId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "task_id", nullable = false)
    @Schema(description = "Задание, для которого предоставлено решение")
    private TaskModel task;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @Schema(description = "Пользователь, предоставивший решение")
    private UserModel user;

    @Column(name = "answer_text", columnDefinition = "TEXT")
    @Schema(description = "Текст ответа", example = "Решение задачи представлено здесь...")
    private String answerText;

    @Size(max = 255)
    @Column(name = "answer_file")
    @Schema(description = "Файл с ответом", example = "/path/to/answer.pdf")
    private String answerFile;

    @NotNull
    @Column(name = "submit_date", nullable = false)
    @Schema(description = "Дата отправки решения", example = "2023-01-10")
    private LocalDate submitDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "solution_status_id", nullable = false)
    @Schema(description = "Статус решения")
    private SolutionStatusModel solutionStatus;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "grade_id")
    @Schema(description = "Оценка за решение")
    private GradeModel grade;

    public SolutionModel() {}

    public SolutionModel(TaskModel task, UserModel user, LocalDate submitDate, SolutionStatusModel solutionStatus) {
        this.task = task;
        this.user = user;
        this.submitDate = submitDate;
        this.solutionStatus = solutionStatus;
    }

    public Long getSolutionId() {
        return solutionId;
    }

    public void setSolutionId(Long solutionId) {
        this.solutionId = solutionId;
    }

    public TaskModel getTask() {
        return task;
    }

    public void setTask(TaskModel task) {
        this.task = task;
    }

    public UserModel getUser() {
        return user;
    }

    public void setUser(UserModel user) {
        this.user = user;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public String getAnswerFile() {
        return answerFile;
    }

    public void setAnswerFile(String answerFile) {
        this.answerFile = answerFile;
    }

    public LocalDate getSubmitDate() {
        return submitDate;
    }

    public void setSubmitDate(LocalDate submitDate) {
        this.submitDate = submitDate;
    }

    public SolutionStatusModel getSolutionStatus() {
        return solutionStatus;
    }

    public void setSolutionStatus(SolutionStatusModel solutionStatus) {
        this.solutionStatus = solutionStatus;
    }

    public GradeModel getGrade() {
        return grade;
    }

    public void setGrade(GradeModel grade) {
        this.grade = grade;
    }
}
