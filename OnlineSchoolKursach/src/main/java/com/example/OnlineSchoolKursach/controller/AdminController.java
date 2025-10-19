package com.example.OnlineSchoolKursach.controller;

import com.example.OnlineSchoolKursach.model.*;
import com.example.OnlineSchoolKursach.repository.*;
import com.example.OnlineSchoolKursach.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/api/admin")
@Tag(name = "Admin Management", description = "API для административных функций")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private EnrollmentStatusRepository enrollmentStatusRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private AuthService authService;

    @GetMapping("/users")
    @Operation(summary = "Получить всех пользователей", description = "Получение списка всех пользователей системы")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список пользователей успешно получен", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = UserModel.class)))
    })
    public ResponseEntity<List<UserModel>> getAllUsers(
            @Parameter(description = "Данные аутентификации") 
            Authentication authentication) {
        try {
            List<UserModel> users = userRepository.findAll();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/categories")
    @Operation(summary = "Получить все категории", description = "Получение списка всех категорий курсов")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список категорий успешно получен", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = CategoryModel.class)))
    })
    public ResponseEntity<List<CategoryModel>> getAllCategories(
            @Parameter(description = "Данные аутентификации") 
            Authentication authentication) {
        try {
            List<CategoryModel> categories = categoryRepository.findAll();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/enrollments")
    @Operation(summary = "Получить все записи на курсы", description = "Получение списка всех записей пользователей на курсы")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список записей успешно получен", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = EnrollmentModel.class)))
    })
    public ResponseEntity<List<EnrollmentModel>> getAllEnrollments(
            @Parameter(description = "Данные аутентификации") 
            Authentication authentication) {
        try {
            List<EnrollmentModel> enrollments = enrollmentRepository.findAll();
            return ResponseEntity.ok(enrollments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/enrollment-statuses")
    @Operation(summary = "Получить все статусы записей", description = "Получение списка всех статусов записей на курсы")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список статусов успешно получен", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = EnrollmentStatusModel.class)))
    })
    public ResponseEntity<List<EnrollmentStatusModel>> getAllEnrollmentStatuses(
            @Parameter(description = "Данные аутентификации") 
            Authentication authentication) {
        try {
            List<EnrollmentStatusModel> statuses = enrollmentStatusRepository.findAll();
            return ResponseEntity.ok(statuses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/enrollments/{enrollmentId}/status")
    @Operation(summary = "Обновить статус записи", description = "Обновление статуса записи на курс")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Статус записи успешно обновлен", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = EnrollmentModel.class))),
            @ApiResponse(responseCode = "404", description = "Запись или статус не найдены")
    })
    public ResponseEntity<EnrollmentModel> updateEnrollmentStatus(
            @Parameter(description = "Данные аутентификации") 
            Authentication authentication,
            @Parameter(description = "ID записи") 
            @PathVariable Long enrollmentId,
            @Parameter(description = "ID нового статуса") 
            @RequestParam Long statusId) {
        try {
            EnrollmentModel enrollment = enrollmentRepository.findById(enrollmentId).orElse(null);
            if (enrollment == null) {
                return ResponseEntity.notFound().build();
            }

            EnrollmentStatusModel status = enrollmentStatusRepository.findById(statusId).orElse(null);
            if (status == null) {
                return ResponseEntity.notFound().build();
            }

            enrollment.setEnrollmentStatus(status);
            EnrollmentModel updatedEnrollment = enrollmentRepository.save(enrollment);

            return ResponseEntity.ok(updatedEnrollment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/files")
    @Operation(summary = "Получить все файлы", description = "Получение списка всех файлов в системе")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список файлов успешно получен", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = FileModel.class)))
    })
    public ResponseEntity<List<FileModel>> getAllFiles(
            @Parameter(description = "Данные аутентификации") 
            Authentication authentication) {
        try {
            List<FileModel> files = fileRepository.findAll();
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}