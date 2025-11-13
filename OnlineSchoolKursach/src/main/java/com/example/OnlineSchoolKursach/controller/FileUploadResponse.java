package com.example.OnlineSchoolKursach.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public class FileUploadResponse {
    @Schema(description = "Идентификатор файла", example = "1")
    private Long id;

    @Schema(description = "Оригинальное имя файла", example = "document.pdf")
    private String originalName;

    @Schema(description = "Размер файла в байтах", example = "1024")
    private Long fileSize;

    @Schema(description = "Тип контента файла", example = "application/pdf")
    private String contentType;

    @Schema(description = "Описание файла", example = "Учебный документ")
    private String description;

    @Schema(description = "Дата загрузки файла", example = "2023-01-01T10:00:00")
    private LocalDateTime uploadDate;

    @Schema(description = "URL для доступа к файлу", example = "http://localhost:9000/school-files/uuid-filename.jpg")
    private String url;

    @Schema(description = "Путь к файлу в MinIO", example = "images/uuid-filename.jpg")
    private String filePath;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}