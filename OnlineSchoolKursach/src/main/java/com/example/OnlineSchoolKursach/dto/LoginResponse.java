package com.example.OnlineSchoolKursach.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ на аутентификацию")
public class LoginResponse {
    @Schema(description = "JWT токен для аутентификации", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;
    
    @Schema(description = "Имя пользователя", example = "user@example.com")
    private String username;
    
    @Schema(description = "Роль пользователя", example = "Студент")
    private String role;

    public LoginResponse() {}

    public LoginResponse(String token, String username, String role) {
        this.token = token;
        this.username = username;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}