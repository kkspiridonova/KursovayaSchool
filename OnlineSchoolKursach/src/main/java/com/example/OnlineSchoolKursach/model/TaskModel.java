package com.example.OnlineSchoolKursach.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Entity
@Table(name = "tasks")
@Schema(description = "Модель задания")
public class TaskModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    @Schema(description = "Идентификатор задания", example = "1")
    private Long taskId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lesson_id", nullable = false)
    @Schema(description = "Урок, к которому относится задание")
    private LessonModel lesson;

    @NotBlank
    @Size(max = 100)
    @Column(name = "title", nullable = false)
    @Schema(description = "Название задания", example = "Домашнее задание по математике")
    private String title;

    @NotBlank
    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    @Schema(description = "Описание задания", example = "Решите задачи 1-10 из учебника")
    private String description;

    @NotNull
    @Column(name = "deadline", nullable = false)
    @Schema(description = "Крайний срок выполнения", example = "2023-01-15")
    private LocalDate deadline;

    @Size(max = 255)
    @Column(name = "attached_file")
    @Schema(description = "Прикрепленный файл", example = "/path/to/task.pdf")
    private String attachedFile;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "task_status_id", nullable = false)
    @Schema(description = "Статус задания")
    private TaskStatusModel taskStatus;

    public TaskModel() {}

    public TaskModel(LessonModel lesson, String title, String description, LocalDate deadline, TaskStatusModel taskStatus) {
        this.lesson = lesson;
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.taskStatus = taskStatus;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public LessonModel getLesson() {
        return lesson;
    }

    public void setLesson(LessonModel lesson) {
        this.lesson = lesson;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public String getAttachedFile() {
        return attachedFile;
    }

    public void setAttachedFile(String attachedFile) {
        this.attachedFile = attachedFile;
    }

    public TaskStatusModel getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(TaskStatusModel taskStatus) {
        this.taskStatus = taskStatus;
    }
}
