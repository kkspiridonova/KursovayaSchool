package com.example.OnlineSchoolKursach.controller;

import com.example.OnlineSchoolKursach.model.UserModel;
import com.example.OnlineSchoolKursach.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/api")
@Tag(name = "Test API", description = "Тестовые endpoints для проверки работы API")
public class TestController {

    @Autowired
    private AuthService authService;

    @GetMapping("/test")
    @Operation(summary = "Тестовый endpoint", description = "Публичный тестовый endpoint для проверки работы API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно"),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    public ResponseEntity<String> test(
            @Parameter(description = "Данные аутентификации") 
            Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String role = authentication.getAuthorities().iterator().next().getAuthority();
            return ResponseEntity.ok("Привет! Вы авторизованы как: " + authentication.getName() + 
                                   " с ролью: " + role);
        }
        return ResponseEntity.ok("Привет! Вы не авторизованы.");
    }

    @GetMapping("/admin/test")
    @Operation(summary = "Тестовый endpoint для админа", 
               description = "Защищенный endpoint, доступный только для администраторов",
               security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Нет прав доступа")
    })
    public ResponseEntity<String> adminTest(
            @Parameter(description = "Данные аутентификации") 
            Authentication authentication) {
        return ResponseEntity.ok("Админ панель работает! Пользователь: " + authentication.getName());
    }

    @GetMapping("/teacher/test")
    @Operation(summary = "Тестовый endpoint для учителя", 
               description = "Защищенный endpoint, доступный только для преподавателей",
               security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Нет прав доступа")
    })
    public ResponseEntity<String> teacherTest(
            @Parameter(description = "Данные аутентификации") 
            Authentication authentication) {
        return ResponseEntity.ok("Панель учителя работает! Пользователь: " + authentication.getName());
    }

    @GetMapping("/student/test")
    @Operation(summary = "Тестовый endpoint для ученика", 
               description = "Защищенный endpoint, доступный только для студентов",
               security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Нет прав доступа")
    })
    public ResponseEntity<String> studentTest(
            @Parameter(description = "Данные аутентификации") 
            Authentication authentication) {
        return ResponseEntity.ok("Панель ученика работает! Пользователь: " + authentication.getName());
    }
}