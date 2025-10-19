package com.example.OnlineSchoolKursach.service;

import com.example.OnlineSchoolKursach.dto.FileDto;
import com.example.OnlineSchoolKursach.model.FileModel;
import com.example.OnlineSchoolKursach.model.UserModel;
import com.example.OnlineSchoolKursach.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class FileService {

    @Autowired
    private MinioFileService minioFileService;

    @Autowired
    private FileRepository fileRepository;

    public FileModel uploadFile(MultipartFile file, UserModel user, String description) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Файл не может быть пустым");
        }

        boolean isImage = isImageFile(file);
        String objectName = minioFileService.uploadFile(file, isImage);

        FileModel fileModel = new FileModel(
                file.getOriginalFilename(),
                objectName,
                file.getSize(),
                file.getContentType(),
                user
        );
        fileModel.setDescription(description);

        return fileRepository.save(fileModel);
    }

    public Resource loadFileAsResource(Long fileId) throws IOException {
        FileModel fileModel = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Файл не найден"));

        try {
            InputStream inputStream = minioFileService.downloadFile(fileModel.getFilePath());
            return new InputStreamResource(inputStream);
        } catch (Exception ex) {
            throw new RuntimeException("Ошибка при загрузке файла", ex);
        }
    }

    public String getFileUrl(String objectName) {
        return minioFileService.getFileUrl(objectName);
    }

    public FileModel getFileInfo(Long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Файл не найден"));
    }

    public List<FileDto> getAllFiles() {
        List<FileModel> files = fileRepository.findAllByOrderByUploadDateDesc();
        return files.stream()
                .map(this::convertToDto)
                .toList();
    }

    public List<FileDto> getFilesByUser(UserModel user) {
        List<FileModel> files = fileRepository.findByUploadedByOrderByUploadDateDesc(user);
        return files.stream()
                .map(this::convertToDto)
                .toList();
    }

    private FileDto convertToDto(FileModel file) {
        return new FileDto(
                file.getId(),
                file.getOriginalName(),
                file.getFileSize(),
                file.getContentType(),
                file.getDescription(),
                file.getUploadDate(),
                file.getUploadedBy().getEmail()
        );
    }

    public void deleteFile(Long fileId, UserModel user) throws IOException {
        FileModel fileModel = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Файл не найден"));

        if (!fileModel.getUploadedBy().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Нет прав для удаления файла");
        }

        minioFileService.deleteFile(fileModel.getFilePath());
        fileRepository.delete(fileModel);
    }

    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && (
            contentType.startsWith("image/") ||
            contentType.equals("image/jpeg") ||
            contentType.equals("image/png") ||
            contentType.equals("image/gif") ||
            contentType.equals("image/webp")
        );
    }
}