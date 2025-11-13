package com.example.OnlineSchoolKursach.controller;

import com.example.OnlineSchoolKursach.service.PasswordResetService;
import com.example.OnlineSchoolKursach.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
public class PasswordResetController {

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/forgot-password")
    @ResponseBody
    public ResponseEntity<Map<String, String>> requestPasswordReset(@RequestParam String email) {
        Map<String, String> response = new HashMap<>();
        try {
            passwordResetService.requestPasswordReset(email);
            response.put("message", "На указанный email было отправлено письмо с инструкциями по восстановлению пароля.");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // Если это ошибка о несуществующем пользователе, вернем конкретное сообщение
            if (e.getMessage() != null && e.getMessage().contains("не найден")) {
                response.put("error", e.getMessage());
            } else {
                response.put("error", "Произошла ошибка при отправке письма. Попробуйте позже.");
            }
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("error", "Произошла ошибка при отправке письма. Попробуйте позже.");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam(required = false) String token, Model model) {
        if (token == null || token.isEmpty()) {
            model.addAttribute("error", "Токен восстановления не указан");
            return "reset-password";
        }

        boolean isValid = passwordResetService.validateToken(token);
        if (!isValid) {
            model.addAttribute("error", "Токен восстановления недействителен или истек");
        } else {
            model.addAttribute("token", token);
        }
        return "reset-password";
    }

    @PostMapping("/forgot-password/me")
    @ResponseBody
    public ResponseEntity<Map<String, String>> requestPasswordResetForCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, String> response = new HashMap<>();
        
        String email = null;
        
        // Попробуем получить email из Spring Security контекста
        if (userDetails != null && userDetails.getUsername() != null) {
            email = userDetails.getUsername();
        } else if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // Если нет в контексте, попробуем извлечь из JWT токена
            try {
                String jwt = authHeader.substring(7);
                email = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                // Игнорируем ошибку, попробуем другой способ
            }
        }
        
        if (email == null || email.isEmpty()) {
            response.put("error", "Вы не авторизованы. Пожалуйста, войдите в систему.");
            return ResponseEntity.status(401).body(response);
        }
        
        try {
            passwordResetService.requestPasswordReset(email);
            response.put("message", "На ваш email (" + email + ") было отправлено письмо с инструкциями по восстановлению пароля.");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("не найден")) {
                response.put("error", e.getMessage());
            } else {
                response.put("error", "Произошла ошибка при отправке письма. Попробуйте позже.");
            }
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("error", "Произошла ошибка при отправке письма. Попробуйте позже.");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/reset-password")
    @ResponseBody
    public ResponseEntity<Map<String, String>> resetPassword(
            @RequestParam String token,
            @RequestParam String password) {
        Map<String, String> response = new HashMap<>();
        
        if (password == null || password.length() < 6) {
            response.put("error", "Пароль должен содержать минимум 6 символов");
            return ResponseEntity.badRequest().body(response);
        }

        boolean success = passwordResetService.resetPassword(token, password);
        if (success) {
            response.put("message", "Пароль успешно изменен. Теперь вы можете войти с новым паролем.");
            return ResponseEntity.ok(response);
        } else {
            response.put("error", "Токен восстановления недействителен или истек");
            return ResponseEntity.badRequest().body(response);
        }
    }
}

