package com.example.OnlineSchoolKursach.controller;

import com.example.OnlineSchoolKursach.dto.FileDto;
import com.example.OnlineSchoolKursach.model.FileModel;
import com.example.OnlineSchoolKursach.model.UserModel;
import com.example.OnlineSchoolKursach.service.AuthService;
import com.example.OnlineSchoolKursach.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/v1/api/files")
@Tag(name = "File Management")
public class FileController {

    @Autowired
    private FileService fileService;

    @Autowired
    private AuthService authService;

    @PostMapping("/upload")
    @Operation(summary = "Загрузить файл")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            Authentication authentication) {
        
        try {
            UserModel user = authService.getUserByEmail(authentication.getName());
            FileModel uploadedFile = fileService.uploadFile(file, user, description);
            return ResponseEntity.ok(uploadedFile);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при загрузке файла: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка: " + e.getMessage());
        }
    }

    @GetMapping("/download/{fileId}")
    @Operation(summary = "Скачать файл")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {
        try {
            FileModel fileInfo = fileService.getFileInfo(fileId);
            Resource resource = fileService.loadFileAsResource(fileId);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(fileInfo.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + fileInfo.getOriginalName() + "\"")
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/list")
    @Operation(summary = "Получить список всех файлов")
    public ResponseEntity<List<FileDto>> getAllFiles() {
        List<FileDto> files = fileService.getAllFiles();
        return ResponseEntity.ok(files);
    }

    @GetMapping("/my")
    @Operation(summary = "Получить мои файлы")
    public ResponseEntity<List<FileDto>> getMyFiles(Authentication authentication) {
        try {
            UserModel user = authService.getUserByEmail(authentication.getName());
            List<FileDto> files = fileService.getFilesByUser(user);
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{fileId}")
    @Operation(summary = "Удалить файл")
    public ResponseEntity<?> deleteFile(@PathVariable Long fileId, Authentication authentication) {
        try {
            UserModel user = authService.getUserByEmail(authentication.getName());
            fileService.deleteFile(fileId, user);
            return ResponseEntity.ok("Файл успешно удален");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при удалении файла: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка: " + e.getMessage());
        }
    }
}
