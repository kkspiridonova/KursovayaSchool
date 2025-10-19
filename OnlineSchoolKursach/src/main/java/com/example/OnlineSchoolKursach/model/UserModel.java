package com.example.OnlineSchoolKursach.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Entity
@Table(name = "users")
@Schema(description = "Модель пользователя")
public class UserModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    @Schema(description = "Идентификатор пользователя", example = "1")
    private Long userId;

    @NotBlank(message = "Имя не должно быть пустым")
    @Size(max = 25, message = "Имя не должно превышать 25 символов")
    @Column(name = "first_name", nullable = false)
    @Schema(description = "Имя пользователя", example = "Иван")
    private String firstName;

    @NotBlank(message = "Фамилия не должна быть пустой")
    @Size(max = 25, message = "Фамилия не должна превышать 25 символов")
    @Column(name = "last_name", nullable = false)
    @Schema(description = "Фамилия пользователя", example = "Иванов")
    private String lastName;

    @Size(max = 25, message = "Отчество не должно превышать 25 символов")
    @Column(name = "middle_name")
    @Schema(description = "Отчество пользователя", example = "Иванович")
    private String middleName;

    @NotBlank(message = "Email не должен быть пустым")
    @Email(message = "Некорректный email")
    @Size(max = 50, message = "Email не должен превышать 50 символов")
    @Column(unique = true, nullable = false)
    @Schema(description = "Email пользователя", example = "user@example.com")
    private String email;

    @NotBlank(message = "Пароль не должен быть пустым")
    @Size(max = 255, message = "Пароль не должен превышать 255 символов")
    @Column(name = "password_hash", nullable = false)
    @Schema(description = "Хэш пароля пользователя", example = "$2a$10$...")
    private String passwordHash;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    @Schema(description = "Роль пользователя")
    private RoleModel role;

    @Column(name = "registration_date", nullable = false)
    @Schema(description = "Дата регистрации пользователя", example = "2023-01-01")
    private LocalDate registrationDate;

    @Column(name = "image_url", length = 1000)
    @Schema(description = "URL изображения профиля пользователя")
    private String imageUrl;

    public UserModel() {
        this.registrationDate = LocalDate.now();
    }

    public UserModel(String firstName, String lastName, String email, String passwordHash, RoleModel role) {
        this();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public RoleModel getRole() {
        return role;
    }

    public void setRole(RoleModel role) {
        this.role = role;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}