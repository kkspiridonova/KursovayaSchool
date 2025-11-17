package com.example.OnlineSchoolKursach.controller;

import com.example.OnlineSchoolKursach.service.AuthService;
import com.example.OnlineSchoolKursach.service.UserSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/api/settings")
@Tag(name = "User Settings", description = "API для управления настройками пользователя")
public class UserSettingsController {

    private static final Logger logger = LoggerFactory.getLogger(UserSettingsController.class);

    @Autowired
    private UserSettingsService userSettingsService;

    @Autowired
    private AuthService authService;

    @GetMapping
    @Operation(summary = "Получить настройки пользователя", description = "Получение всех настроек текущего пользователя")
    public ResponseEntity<Map<String, Object>> getSettings(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).build();
            }

            Long userId = authService.getUserByEmail(authentication.getName()).getUserId();
            Map<String, Object> settings = userSettingsService.getSettingsAsMap(userId);
            return ResponseEntity.ok(settings);
        } catch (Exception e) {
            logger.error("Error getting user settings: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    @Operation(summary = "Сохранить настройки пользователя", description = "Сохранение настроек текущего пользователя")
    public ResponseEntity<Map<String, Object>> saveSettings(
            @RequestBody Map<String, Object> settingsData,
            Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).build();
            }

            Long userId = authService.getUserByEmail(authentication.getName()).getUserId();
            userSettingsService.saveSettings(userId, settingsData);
            Map<String, Object> updatedSettings = userSettingsService.getSettingsAsMap(userId);
            return ResponseEntity.ok(updatedSettings);
        } catch (Exception e) {
            logger.error("Error saving user settings: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
}

