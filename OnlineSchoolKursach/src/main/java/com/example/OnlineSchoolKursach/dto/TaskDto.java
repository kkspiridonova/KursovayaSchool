package com.example.OnlineSchoolKursach.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Schema(description = "DTO для создания задания")
public class TaskDto {

    @NotBlank
    @Schema(description = "Название задания", example = "Домашнее задание по математике")
    private String title;

    @NotBlank
    @Schema(description = "Описание задания", example = "Решите задачи 1-10 из учебника")
    private String description;

    @NotNull
    @Schema(description = "Крайний срок выполнения", example = "2023-01-15")
    private LocalDate deadline;

    @Schema(description = "Прикрепленный файл (MultipartFile)")
    private MultipartFile attachedFile;
    
    @Schema(description = "Путь к прикрепленному файлу (для редактирования)", example = "/path/to/task.pdf")
    private String attachedFilePath;

    public TaskDto() {}

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

    public MultipartFile getAttachedFile() {
        return attachedFile;
    }

    public void setAttachedFile(MultipartFile attachedFile) {
        this.attachedFile = attachedFile;
    }

    public String getAttachedFilePath() {
        return attachedFilePath;
    }

    public void setAttachedFilePath(String attachedFilePath) {
        this.attachedFilePath = attachedFilePath;
    }
}