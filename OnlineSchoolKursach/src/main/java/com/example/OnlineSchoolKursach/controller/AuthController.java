package com.example.OnlineSchoolKursach.controller;

import com.example.OnlineSchoolKursach.dto.LoginRequest;
import com.example.OnlineSchoolKursach.dto.LoginResponse;
import com.example.OnlineSchoolKursach.model.UserModel;
import com.example.OnlineSchoolKursach.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/api/auth")
@Tag(name = "Authentication", description = "API для аутентификации пользователей")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Вход в систему", description = "Аутентификация пользователя по email и паролю")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешная аутентификация", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "Неверные учетные данные")
    })
    public ResponseEntity<LoginResponse> login(
            @Parameter(description = "Запрос на аутентификацию") 
            @Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(summary = "Регистрация пользователя", description = "Создание новой учетной записи пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пользователь успешно зарегистрирован", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = UserModel.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные пользователя")
    })
    public ResponseEntity<UserModel> register(
            @Parameter(description = "Данные нового пользователя") 
            @Valid @RequestBody UserModel user) {
        UserModel createdUser = authService.register(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @GetMapping("/me")
    @Operation(summary = "Текущий пользователь", description = "Получение информации о текущем аутентифицированном пользователе")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Информация о пользователе получена", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = UserModel.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован")
    })
    public ResponseEntity<UserModel> me(
            @Parameter(description = "Данные аутентифицированного пользователя") 
            @AuthenticationPrincipal UserDetails principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return authService.getUserByEmail(principal.getUsername()) != null
                ? ResponseEntity.ok(authService.getUserByEmail(principal.getUsername()))
                : ResponseEntity.notFound().build();
    }

    @PutMapping("/me")
    @Operation(summary = "Обновить профиль", description = "Обновление информации о текущем пользователе")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Профиль успешно обновлен", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = UserModel.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка в запросе"),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован")
    })
    public ResponseEntity<UserModel> updateProfile(
            @Parameter(description = "Обновленные данные пользователя") 
            @RequestBody UserModel updatedUser,
            @Parameter(description = "Данные аутентифицированного пользователя") 
            @AuthenticationPrincipal UserDetails principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            UserModel updated = authService.updateUserProfile(principal.getUsername(), updatedUser);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}