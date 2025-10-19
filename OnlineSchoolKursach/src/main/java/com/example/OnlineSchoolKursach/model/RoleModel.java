package com.example.OnlineSchoolKursach.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "roles")
@Schema(description = "Модель роли пользователя")
public class RoleModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    @Schema(description = "Идентификатор роли", example = "1")
    private Long roleId;

    @NotBlank
    @Size(max = 30)
    @Column(name = "role_name", unique = true, nullable = false)
    @Schema(description = "Название роли", example = "Студент")
    private String roleName;

    public RoleModel() {}

    public RoleModel(String roleName) {
        this.roleName = roleName;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}