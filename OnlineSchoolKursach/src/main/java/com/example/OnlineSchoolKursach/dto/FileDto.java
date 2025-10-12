package com.example.OnlineSchoolKursach.dto;

import java.time.LocalDateTime;

public class FileDto {
    private Long id;
    private String originalName;
    private Long fileSize;
    private String contentType;
    private String description;
    private LocalDateTime uploadDate;
    private String uploaderEmail;

    public FileDto() {}

    public FileDto(Long id, String originalName, Long fileSize, String contentType, 
                   String description, LocalDateTime uploadDate, String uploaderEmail) {
        this.id = id;
        this.originalName = originalName;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.description = description;
        this.uploadDate = uploadDate;
        this.uploaderEmail = uploaderEmail;
    }

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

    public String getUploaderEmail() {
        return uploaderEmail;
    }

    public void setUploaderEmail(String uploaderEmail) {
        this.uploaderEmail = uploaderEmail;
    }

    public String getUploaderUsername() {
        return uploaderEmail;
    }

    public void setUploaderUsername(String uploaderUsername) {
        this.uploaderEmail = uploaderUsername;
    }
}


