package com.example.OnlineSchoolKursach.controller;

import com.example.OnlineSchoolKursach.dto.FileDto;
import com.example.OnlineSchoolKursach.model.FileModel;
import com.example.OnlineSchoolKursach.model.UserModel;
import com.example.OnlineSchoolKursach.service.AuthService;
import com.example.OnlineSchoolKursach.service.FileService;
import com.example.OnlineSchoolKursach.service.MinioFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/v1/api/files")
@Tag(name = "File Management", description = "API для управления файлами")
public class FileController {

    @Autowired
    private FileService fileService;

    @Autowired
    private AuthService authService;

    @Autowired
    private MinioFileService minioFileService;

    @PostMapping("/upload")
    @Operation(summary = "Загрузить файл", description = "Загрузка файла в систему")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Файл успешно загружен", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = FileModel.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка в запросе"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<?> uploadFile(
            @Parameter(description = "Файл для загрузки") 
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Описание файла (необязательно)") 
            @RequestParam(value = "description", required = false) String description,
            @Parameter(description = "Данные аутентификации") 
            Authentication authentication) {
        
        try {
            UserModel user = authService.getUserByEmail(authentication.getName());
            FileModel uploadedFile = fileService.uploadFile(file, user, description);
            
            FileUploadResponse response = new FileUploadResponse();
            response.setId(uploadedFile.getId());
            response.setOriginalName(uploadedFile.getOriginalName());
            response.setFileSize(uploadedFile.getFileSize());
            response.setContentType(uploadedFile.getContentType());
            response.setDescription(uploadedFile.getDescription());
            response.setUploadDate(uploadedFile.getUploadDate());
            response.setUrl(fileService.getFileUrl(uploadedFile.getFilePath()));
            response.setFilePath(uploadedFile.getFilePath());
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при загрузке файла: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка: " + e.getMessage());
        }
    }

    @PostMapping("/upload-solution")
    @Operation(summary = "Загрузить файл решения", description = "Загрузка файла решения в MinIO (папка solutions/)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Файл успешно загружен"),
            @ApiResponse(responseCode = "400", description = "Ошибка в запросе"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<?> uploadSolutionFile(
            @Parameter(description = "Файл для загрузки") 
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Данные аутентификации") 
            Authentication authentication) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Файл не может быть пустым");
            }

            String filePath = minioFileService.uploadFile(file, "solution");

            java.util.Map<String, String> response = new java.util.HashMap<>();
            response.put("filePath", filePath);
            response.put("originalName", file.getOriginalFilename());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при загрузке файла: " + e.getMessage());
        }
    }

    @GetMapping("/download/{fileId}")
    @Operation(summary = "Скачать файл", description = "Скачивание файла по идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Файл успешно скачан"),
            @ApiResponse(responseCode = "404", description = "Файл не найден"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "Идентификатор файла") 
            @PathVariable Long fileId) {
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
    @Operation(summary = "Получить список всех файлов", description = "Получение списка всех доступных файлов")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список файлов успешно получен", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = FileDto.class)))
    })
    public ResponseEntity<List<FileDto>> getAllFiles() {
        List<FileDto> files = fileService.getAllFiles();
        return ResponseEntity.ok(files);
    }

    @GetMapping("/my")
    @Operation(summary = "Получить мои файлы", description = "Получение списка файлов, загруженных текущим пользователем")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список файлов успешно получен", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = FileDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка в запросе")
    })
    public ResponseEntity<List<FileDto>> getMyFiles(
            @Parameter(description = "Данные аутентификации") 
            Authentication authentication) {
        try {
            UserModel user = authService.getUserByEmail(authentication.getName());
            List<FileDto> files = fileService.getFilesByUser(user);
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{fileId}")
    @Operation(summary = "Удалить файл", description = "Удаление файла по идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Файл успешно удален"),
            @ApiResponse(responseCode = "400", description = "Ошибка в запросе"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<?> deleteFile(
            @Parameter(description = "Идентификатор файла") 
            @PathVariable Long fileId, 
            @Parameter(description = "Данные аутентификации") 
            Authentication authentication) {
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

    @GetMapping("/image")
    @Operation(summary = "Получить изображение", description = "Получение изображения из MinIO по пути")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Изображение успешно получено"),
            @ApiResponse(responseCode = "404", description = "Изображение не найдено"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<Resource> getImage(
            @Parameter(description = "Путь к изображению в MinIO") 
            @RequestParam("path") String path) {
        try {
            if (path == null || path.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            String imagePath = java.net.URLDecoder.decode(path, "UTF-8");

            InputStream inputStream = fileService.getFileInputStream(imagePath);
            Resource resource = new InputStreamResource(inputStream);

            String contentType = "image/jpeg";
            if (imagePath.toLowerCase().endsWith(".png")) {
                contentType = "image/png";
            } else if (imagePath.toLowerCase().endsWith(".gif")) {
                contentType = "image/gif";
            } else if (imagePath.toLowerCase().endsWith(".webp")) {
                contentType = "image/webp";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/download")
    @Operation(summary = "Скачать файл из MinIO", description = "Скачивание файла из MinIO по пути")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Файл успешно скачан"),
            @ApiResponse(responseCode = "404", description = "Файл не найден"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<Resource> downloadFileFromMinio(
            @Parameter(description = "Путь к файлу в MinIO") 
            @RequestParam("path") String path) {
        try {
            if (path == null || path.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            String filePath = java.net.URLDecoder.decode(path, java.nio.charset.StandardCharsets.UTF_8);

            InputStream inputStream = fileService.getFileInputStream(filePath);
            Resource resource = new InputStreamResource(inputStream);

            String filename = filePath.substring(filePath.lastIndexOf("/") + 1);
            if (filename.isEmpty()) {
                filename = "file";
            }

            String contentType = "application/octet-stream";
            String lowerPath = filePath.toLowerCase();
            if (lowerPath.endsWith(".pdf")) {
                contentType = "application/pdf";
            } else if (lowerPath.endsWith(".doc") || lowerPath.endsWith(".docx")) {
                contentType = "application/msword";
            } else if (lowerPath.endsWith(".xls") || lowerPath.endsWith(".xlsx")) {
                contentType = "application/vnd.ms-excel";
            } else if (lowerPath.endsWith(".txt")) {
                contentType = "text/plain";
            } else if (lowerPath.endsWith(".zip")) {
                contentType = "application/zip";
            } else if (lowerPath.endsWith(".jpg") || lowerPath.endsWith(".jpeg")) {
                contentType = "image/jpeg";
            } else if (lowerPath.endsWith(".png")) {
                contentType = "image/png";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}