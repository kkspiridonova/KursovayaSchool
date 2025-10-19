package com.example.OnlineSchoolKursach.model;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "files")
@Schema(description = "Модель файла")
public class FileModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Идентификатор файла", example = "1")
    private Long id;

    @NotBlank
    @Size(max = 255)
    @Column(name = "original_name", nullable = false)
    @Schema(description = "Оригинальное имя файла", example = "document.pdf")
    private String originalName;

    @NotBlank
    @Size(max = 255)
    @Column(name = "file_path", nullable = false)
    @Schema(description = "Путь к файлу в хранилище", example = "/path/to/file/document.pdf")
    private String filePath;

    @Column(name = "file_size")
    @Schema(description = "Размер файла в байтах", example = "1024")
    private Long fileSize;

    @Size(max = 100)
    @Column(name = "content_type")
    @Schema(description = "Тип контента файла", example = "application/pdf")
    private String contentType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "uploaded_by", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    @Schema(description = "Пользователь, загрузивший файл")
    private UserModel uploadedBy;

    @Column(name = "upload_date", nullable = false)
    @Schema(description = "Дата загрузки файла", example = "2023-01-01T10:00:00")
    private LocalDateTime uploadDate;

    @Size(max = 500)
    @Column(name = "description")
    @Schema(description = "Описание файла", example = "Учебный документ")
    private String description;

    public FileModel() {
        this.uploadDate = LocalDateTime.now();
    }

    public FileModel(String originalName, String filePath, Long fileSize, String contentType, UserModel uploadedBy) {
        this();
        this.originalName = originalName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.uploadedBy = uploadedBy;
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

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
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

    public UserModel getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(UserModel uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}